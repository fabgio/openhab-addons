/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.openhab.binding.meross.internal.command.Command;
import org.openhab.binding.meross.internal.dto.MQTTResponse;
import org.openhab.binding.meross.internal.factory.AbstractFactory;
import org.openhab.binding.meross.internal.factory.FactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossManager} class is responsible for implementing functionalities to interact with appliances
 *
 *
 * @author Giovanni Fabiani - Initial contribution
 */
public class MerossManager {
    private static final Logger logger = LoggerFactory.getLogger(MerossManager.class);
    private final MerossHttpConnector merossHttpConnector;

    private MerossManager(MerossHttpConnector merossHttpConnector) {
        this.merossHttpConnector = merossHttpConnector;
    }

    public static MerossManager createMerossManager(MerossHttpConnector merossHttpConnector) {
        return new MerossManager(merossHttpConnector);
    }

    private void initializeHttpConnector(String deviceName) {
        String clientId = MerossMqttConnector.buildClientId();
        MerossMqttConnector.setClientId(clientId);
        String userId = Optional.ofNullable(merossHttpConnector.getCredentials().userId()).orElseThrow(() -> {
            logger.debug("userId is null");
            return new IllegalStateException("userId is null");
        });
        MerossMqttConnector.setUserId(userId);
        String key = Optional.ofNullable(merossHttpConnector.getCredentials().key()).orElseThrow(() -> {
            logger.debug("key is null");
            return new IllegalStateException("key is null");
        });
        MerossMqttConnector.setKey(key);
        String brokerAddress = Optional.ofNullable(merossHttpConnector.getCredentials().mqttDomain())
                .orElseThrow(() -> {
                    logger.debug("brokerAddress is null");
                    return new IllegalStateException("brokerAddress is null");
                });
        MerossMqttConnector.setBrokerAddress(brokerAddress);
        String deviceUUID = Optional.ofNullable(merossHttpConnector.getDevUUIDByDevName(deviceName)).orElseThrow(() -> {
            logger.debug("deviceUUID is null");
            return new IllegalStateException("deviceUUID is null");
        });
        MerossMqttConnector.setDestinationDeviceUUID(deviceUUID);
    }

    /**
     * Executes a command on the device, set commandMode e,g. ON or OFF and returns data
     * 
     * @param deviceName The device's name
     * @param commandType The command type
     * @param commandMode The command Mode
     * @return Response record
     */
    public MQTTResponse executeCommand(String deviceName, String commandType, String commandMode) {
        initializeHttpConnector(deviceName);
        String deviceUUID = merossHttpConnector.getDevUUIDByDevName(deviceName);
        String requestTopic = MerossMqttConnector.buildDeviceRequestTopic(deviceUUID);
        byte[] systemAbilityMessage = MerossMqttConnector.buildMqttMessage("GET",
                MerossEnum.Namespace.SYSTEM_ABILITY.getValue(), Collections.emptyMap());
        String systemAbilityPublishesMessage = MerossMqttConnector.publishMqttMessage(systemAbilityMessage,
                requestTopic);
        AbstractFactory abstractFactory = FactoryProvider.getFactory(commandType);
        Command command = abstractFactory.commandMode(commandMode);
        byte[] commandMessage = command.commandType(commandType);
        String method = getMethod(commandMessage, requestTopic);
        HashSet<String> abilities = abilityResponse(systemAbilityPublishesMessage);
        if (!abilities.contains(MerossEnum.Namespace.getAbilityValueByName(commandType))) {
            throw new IllegalStateException("Command type not supported");
        }
        if (!abilities.contains(MerossEnum.Namespace.getAbilityValueByName(commandType))) {
            throw new IllegalStateException("Command type not supported");
        }
        return new MQTTResponse(Map.of("method", method));
    }

    public MQTTResponse executeCommand(String deviceName, String commandType) {
        initializeHttpConnector(deviceName);
        String deviceUUID = merossHttpConnector.getDevUUIDByDevName(deviceName);
        String requestTopic = MerossMqttConnector.buildDeviceRequestTopic(deviceUUID);
        byte[] systemAllMessage = MerossMqttConnector.buildMqttMessage("GET",
                MerossEnum.Namespace.SYSTEM_ALL.getValue(), Collections.emptyMap());
        String systemAllPublishesMessage = MerossMqttConnector.publishMqttMessage(systemAllMessage, requestTopic);
        byte[] systemAbilityMessage = MerossMqttConnector.buildMqttMessage("GET",
                MerossEnum.Namespace.SYSTEM_ABILITY.getValue(), Collections.emptyMap());
        String systemAbilityPublishesMessage = MerossMqttConnector.publishMqttMessage(systemAbilityMessage,
                requestTopic);
        HashSet<String> abilities = abilityResponse(systemAbilityPublishesMessage);
        if (!abilities.contains(MerossEnum.Namespace.getAbilityValueByName(commandType))) {
            throw new RuntimeException("Command type not supported");
        }
        return getResponse(commandType, systemAllPublishesMessage);
    }

    private HashSet<String> abilityResponse(String jsonString) {
        JsonElement digestElement = JsonParser.parseString(jsonString);
        String abilityString = digestElement.getAsJsonObject().get("payload").getAsJsonObject().get("ability")
                .getAsJsonObject().toString();
        TypeToken<HashMap<String, HashMap<String, String>>> type = new TypeToken<>() {
        };
        HashMap<String, HashMap<String, String>> response = new Gson().fromJson(abilityString, type);
        return new HashSet<>(response.keySet());
    }

    private static String getMethod(byte[] commandMessage, String requestTopic) {
        String publishMqttMessage = MerossMqttConnector.publishMqttMessage(commandMessage, requestTopic);
        JsonElement jsonElement = JsonParser.parseString(publishMqttMessage);
        return jsonElement.getAsJsonObject().getAsJsonObject("header").get("method").getAsString();
    }

    private MQTTResponse getResponse(String commandType, String systemAllPublishesMessage) {
        return switch (commandType) {
            case "CONTROL_TOGGLEX" -> togglexResponse(systemAllPublishesMessage);
            default -> throw new IllegalStateException("Unexpected commandType: " + commandType);
        };
    }

    private MQTTResponse togglexResponse(String jsonString) {
        JsonElement jsonElement = JsonParser.parseString(jsonString);
        JsonArray togglexJsonArray = jsonElement.getAsJsonObject().getAsJsonObject().get("payload").getAsJsonObject()
                .get("all").getAsJsonObject().get("digest").getAsJsonObject().get("togglex").getAsJsonArray();
        String method = jsonElement.getAsJsonObject().getAsJsonObject("header").get("method").getAsString();
        int channel = togglexJsonArray.get(0).getAsJsonObject().getAsJsonPrimitive("channel").getAsInt();
        int onoff = togglexJsonArray.get(0).getAsJsonObject().getAsJsonPrimitive("onoff").getAsInt();
        long lmTime = togglexJsonArray.get(0).getAsJsonObject().getAsJsonPrimitive("lmTime").getAsLong();
        return new MQTTResponse(Map.of("method", method, "channel", channel, "onoff", onoff, "lmTime", lmTime));
    }
}
