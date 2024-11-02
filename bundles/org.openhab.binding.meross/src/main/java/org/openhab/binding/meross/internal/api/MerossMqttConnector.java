/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.meross.internal.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;

/**
 * The {@link MerossMqttConnector} class is responsible for building
 * and publishing MQTT messages along with connecting to the Meross broker.
 *
 * @author Giovanni Fabiani - Initial contribution
 */

public class MerossMqttConnector {
    private final static Logger logger = LoggerFactory.getLogger(MerossMqttConnector.class);
    private static final int SECURE_WEB_SOCKET_PORT = 443;
    private static final int RECEPTION_TIMEOUT_SECONDS = 60;
    private static final int KEEP_ALIVE_SECONDS = 60;
    private static String brokerAddress;
    private static String userId;
    private static String clientId;
    private static String key;
    private static String destinationDeviceUUID;

    /**
     * @param message The mqtt message
     * @param requestTopic The request topic
     * @return The mqtt response
     */
    static synchronized String publishMqttMessage(byte[] message, String requestTopic) {
        String clearPassword = "%s%s".formatted(userId, key);
        String hashedPassword = MD5Util.getMD5String(clearPassword);

        Mqtt5BlockingClient client = Mqtt5Client.builder().identifier(clientId).serverHost(brokerAddress)
                .serverPort(SECURE_WEB_SOCKET_PORT).sslWithDefaultConfig().buildBlocking();

        Mqtt5ConnAck connAck = client.connectWith().cleanStart(false).keepAlive(KEEP_ALIVE_SECONDS).simpleAuth()
                .username(userId).password(hashedPassword.getBytes(StandardCharsets.UTF_8)).applySimpleAuth().send();
        if (connAck.getReasonCode().getCode() != Mqtt5ConnAckReasonCode.SUCCESS.getCode()) {
            if (connAck.getReasonString().isPresent()) {
                logger.error("Connection failed: {}", connAck.getReasonString().get());
                return null;
            }
        }

        Mqtt5Subscribe subscribeMessage = Mqtt5Subscribe.builder().addSubscription().topicFilter(buildClientUserTopic())
                .qos(MqttQos.AT_LEAST_ONCE).applySubscription().addSubscription()
                .topicFilter(buildClientResponseTopic()).qos(MqttQos.AT_LEAST_ONCE).applySubscription().build();

        client.subscribe(subscribeMessage);

        Mqtt5Publish publishMessage = Mqtt5Publish.builder().topic(requestTopic).qos(MqttQos.AT_MOST_ONCE)
                .payload(message).build();

        client.publish(publishMessage);

        String incomingResponse = null;
        try (final Mqtt5BlockingClient.Mqtt5Publishes publishes = client
                .publishes(MqttGlobalPublishFilter.SUBSCRIBED)) {
            Optional<Mqtt5Publish> publishesResponse = publishes.receive(RECEPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (publishesResponse.isPresent()) {
                Mqtt5Publish mqtt5PublishResponse = publishesResponse.get();
                if (mqtt5PublishResponse.getPayload().isPresent()) {
                    incomingResponse = StandardCharsets.UTF_8.decode(mqtt5PublishResponse.getPayload().get())
                            .toString();
                } else {
                    logger.error("Received a  MQTT message without a payload");
                }
            } else {
                logger.warn("Did not receive MQTT message within timeout");
            }
        } catch (InterruptedException e) {
            logger.debug("InterruptedException: {}", e.getMessage());

        }
        client.disconnect();
        if (incomingResponse != null) {
            return incomingResponse;
        }
        return "";
    }

    /**
     * @param method The method
     * @param namespace The namespace
     * @param payload The payload
     * @return the message
     */
    public static byte[] buildMqttMessage(String method, String namespace, Map<String, Object> payload) {
        int timestamp = Math.round(Instant.now().getEpochSecond());
        String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String messageId = MD5Util.getMD5String(randomString.toLowerCase());
        String signatureToHash = "%s%s%d".formatted(messageId, key, timestamp);
        String signature = MD5Util.getMD5String(signatureToHash).toLowerCase();
        Map<String, Object> headerMap = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();
        headerMap.put("from", buildClientResponseTopic());
        headerMap.put("messageId", messageId);
        headerMap.put("method", method);
        headerMap.put("namespace", namespace);
        headerMap.put("payloadVersion", 1);
        headerMap.put("sign", signature);
        headerMap.put("timestamp", timestamp);
        headerMap.put("triggerSrc", "Android");
        headerMap.put("uuid", destinationDeviceUUID);
        dataMap.put("header", headerMap);
        dataMap.put("payload", payload);
        String jsonString = new Gson().toJson(dataMap);
        return StandardCharsets.UTF_8.encode(jsonString).array();
    }

    /**
     * In general, the Meross App subscribes to this topic in order to update its state as events happen on the physical
     * device.
     *
     * @return The client user topic
     */
    public static String buildClientUserTopic() {
        return "/app/" + getUserId() + "/subscribe";
    }

    public static String buildAppId() {
        String randomString = "API" + UUID.randomUUID();
        String encodedString = StandardCharsets.UTF_8.encode(randomString).toString();
        return MD5Util.getMD5String(encodedString);
    }

    /**
     * App command.
     * It is the topic to which the Meross App subscribes. It is used by the app to receive the response to commands
     * sent to the appliance
     *
     * @return The response topic
     */
    public static String buildClientResponseTopic() {
        return "/app/" + getUserId() + "-" + buildAppId() + "/subscribe";
    }

    public static String buildClientId() {
        return "app:" + buildAppId();
    }

    /**
     * App command.
     *
     * @param deviceUUID The device UUID
     * @return The topic to be published
     */

    public static String buildDeviceRequestTopic(String deviceUUID) {
        return "/appliance/" + deviceUUID + "/subscribe";
    }

    public static void setUserId(String userId) {
        MerossMqttConnector.userId = userId;
    }

    public static void setClientId(String clientId) {
        MerossMqttConnector.clientId = clientId;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setBrokerAddress(String brokerAddress) {
        MerossMqttConnector.brokerAddress = brokerAddress;
    }

    public static void setKey(String key) {
        MerossMqttConnector.key = key;
    }

    public static void setDestinationDeviceUUID(String destinationDeviceUUID) {
        MerossMqttConnector.destinationDeviceUUID = destinationDeviceUUID;
    }
}
