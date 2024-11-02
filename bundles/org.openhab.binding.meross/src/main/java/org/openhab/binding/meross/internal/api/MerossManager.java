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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.command.Command;
import org.openhab.binding.meross.internal.dto.MQTT;
import org.openhab.binding.meross.internal.factory.ModeFactory;
import org.openhab.binding.meross.internal.factory.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossManager} class is responsible for implementing general functionalities to interact with
 * appliances
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossManager {
    private static final Logger logger = LoggerFactory.getLogger(MerossManager.class);
    final MerossHttpConnector merossHttpConnector;

    public MerossManager(MerossHttpConnector merossHttpConnector) {
        this.merossHttpConnector = merossHttpConnector;
    }

    public void initializeMerossMqttConnector() {
        final var credentials = merossHttpConnector.raedCredentials();
        String clientId = MQTT.buildClientId();
        MQTT.setClientId(clientId);
        if (credentials == null) {
            logger.debug("No credentials found");
        } else {
            String userId = credentials.userId();
            MQTT.setUserId(userId);
            String key = merossHttpConnector.raedCredentials().key();
            MQTT.setKey(key);
            String brokerAddress = merossHttpConnector.raedCredentials().mqttDomain();
            MQTT.setBrokerAddress(brokerAddress);
        }
    }

    /**
     * @param deviceName The device name
     * @param commandType The command type
     * @param commandMode The command Mode
     * @return The MQTT response
     */

    public void executeCommand(String deviceName, String commandType, String commandMode) {
        String uuid = merossHttpConnector.getDevUUIDByDevName(deviceName);
        if (uuid != null && uuid.isEmpty()) {
            logger.error("No device found with name {}", deviceName);
            return;
        }
        initializeMerossMqttConnector();
        String deviceUUID = merossHttpConnector.getDevUUIDByDevName(deviceName);

        MQTT.setDestinationDeviceUUID(deviceUUID);

        String requestTopic = MQTT.buildDeviceRequestTopic(deviceUUID);
        ModeFactory modeFactory = TypeFactory.getFactory(commandType);
        Command command = modeFactory.commandMode(commandMode);
        byte[] commandMessage = command.commandType(commandType);
        if (!getAbilities(deviceName).contains(MerossEnum.Namespace.getAbilityValueByName(commandType))) {
            logger.warn("Command {} not supported", commandType);
        }
        MerossMqttConnector.publishMqttMessage(commandMessage, requestTopic);
    }

    public String onlineStatus(String deviceName) {
        String systemAll = getSystemAll(deviceName);
        JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(systemAll)).getAsJsonObject();
        final JsonObject asJsonObject = jsonObject.getAsJsonObject("payload").getAsJsonObject("all")
                .getAsJsonObject("system").getAsJsonObject("online");
        Optional<JsonElement> jsonElement = Optional.of(asJsonObject.get("status"));
        return jsonElement.get().getAsString();
    }

    public String togglexOnOffStatus(String deviceName) {
        String systemAll = getSystemAll(deviceName);
        JsonObject jsonObject = JsonParser.parseString(Objects.requireNonNull(systemAll)).getAsJsonObject();
        JsonArray togglexArray = jsonObject.getAsJsonObject("payload").getAsJsonObject("all").getAsJsonObject("digest")
                .getAsJsonArray("togglex");
        Optional<JsonElement> jsonElement = Optional.of(togglexArray.get(0).getAsJsonObject().get("onoff"));
        return jsonElement.get().toString();
    }

    public String getSystemAll(String deviceName) {
        initializeMerossMqttConnector();
        String uuid = merossHttpConnector.getDevUUIDByDevName(deviceName);
        if (uuid != null && uuid.isEmpty()) {
            logger.error("No device found with name {}", deviceName);
            return "";
        }
        String requestTopic = null;
        if (uuid != null) {
            requestTopic = MQTT.buildDeviceRequestTopic(uuid);
        }
        byte[] systemAllMessage = MQTT.buildMqttMessage("GET", MerossEnum.Namespace.SYSTEM_ALL.value(),
                Collections.emptyMap());
        String s = null;
        if (requestTopic != null) {
            s = MerossMqttConnector.publishMqttMessage(systemAllMessage, requestTopic);
        }
        if (s != null && !s.isEmpty()) {
            return s;
        }

        return "";
    }

    private @Nullable HashSet<String> getAbilities(String deviceName) {
        initializeMerossMqttConnector();
        String uuid = merossHttpConnector.getDevUUIDByDevName(deviceName);
        if (uuid.isEmpty()) {
            return null;
        }
        String requestTopic = MQTT.buildDeviceRequestTopic(uuid);
        byte[] systemAbilityMessage = MQTT.buildMqttMessage("GET", MerossEnum.Namespace.SYSTEM_ABILITY.value(),
                Collections.emptyMap());
        JsonElement digestElement = null;
        if (requestTopic != null) {
            digestElement = JsonParser.parseString(
                    Objects.requireNonNull(MerossMqttConnector.publishMqttMessage(systemAbilityMessage, requestTopic)));
        }
        String abilityString = digestElement.getAsJsonObject().get("payload").getAsJsonObject().get("ability")
                .getAsJsonObject().toString();
        TypeToken<HashMap<String, HashMap<String, String>>> type = new TypeToken<>() {
        };
        HashMap<String, HashMap<String, String>> abilities = new Gson().fromJson(abilityString, type);
        return new HashSet<>(abilities.keySet());
    }

    public record data(String uuid) {
    }
}
