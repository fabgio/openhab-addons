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

import java.util.*;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.command.Command;
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

public class MerossManager {
    private static final Logger logger = LoggerFactory.getLogger(MerossManager.class);
    private final MerossHttpConnector merossHttpConnector;

    public MerossManager(MerossHttpConnector merossHttpConnector) {
        this.merossHttpConnector = merossHttpConnector;
    }

    public void initializeMerossMqttConnector() {
        final var credentials = merossHttpConnector.raedCredentials();
        String clientId = MerossMqttConnector.buildClientId();
        MerossMqttConnector.setClientId(clientId);
        if (credentials == null) {
            logger.debug("No credentials found");
        } else {
            String userId = credentials.userId();
            MerossMqttConnector.setUserId(userId);
            String key = merossHttpConnector.raedCredentials().key();
            MerossMqttConnector.setKey(key);
            String brokerAddress = merossHttpConnector.raedCredentials().mqttDomain();
            MerossMqttConnector.setBrokerAddress(brokerAddress);
        }
    }

    /**
     * @param deviceName The device name
     * @param commandType The command type
     * @param commandMode The command Mode
     * @return The MQTT response
     */

    public void executeCommand(String deviceName, String commandType, String commandMode) {
        initializeMerossMqttConnector();
        String deviceUUID = Objects.requireNonNull(merossHttpConnector.getDevUUIDByDevName(deviceName));
        MerossMqttConnector.setDestinationDeviceUUID(deviceUUID);
        String requestTopic = MerossMqttConnector.buildDeviceRequestTopic(deviceUUID);
        ModeFactory modeFactory = TypeFactory.getFactory(commandType);
        Command command = modeFactory.commandMode(commandMode);
        byte[] commandMessage = command.commandType(commandType);
        if (!getAbilities(deviceName).contains(MerossEnum.Namespace.getAbilityValueByName(commandType))) {
            logger.warn("Command {} not supported", commandType);
        }
        MerossMqttConnector.publishMqttMessage(commandMessage, requestTopic);
    }

    public int onlineStatus(String deviceName) {
        @Nullable
        JsonElement jsonElement = null;
        final String systemAll = getSystemAll(deviceName);
        if (systemAll == null) {
            logger.warn("SystemAll is null");
        } else {
            JsonObject jsonObject = JsonParser.parseString(systemAll).getAsJsonObject();
            final JsonObject asJsonObject = jsonObject.getAsJsonObject("payload").getAsJsonObject("all")
                    .getAsJsonObject("system").getAsJsonObject("online");
            jsonElement = asJsonObject.get("status");
        }
        return jsonElement.getAsInt();
    }

    public int togglexOnOffStatus(String deviceName) {
        @Nullable
        JsonObject togglexObject = null;
        final String systemAll = getSystemAll(deviceName);
        if (systemAll == null) {
            logger.warn("SystemAll is null");
        } else {
            JsonObject jsonObject = JsonParser.parseString(systemAll).getAsJsonObject();
            JsonArray togglexArray = jsonObject.getAsJsonObject("payload").getAsJsonObject("all")
                    .getAsJsonObject("digest").getAsJsonArray("togglex");
            togglexObject = togglexArray.get(0).getAsJsonObject();
        }
        return togglexObject.get("onoff").getAsInt();
    }

    String getSystemAll(String deviceName) {
        initializeMerossMqttConnector();
        String requestTopic = MerossMqttConnector
                .buildDeviceRequestTopic(merossHttpConnector.getDevUUIDByDevName(deviceName));
        byte[] systemAllMessage = MerossMqttConnector.buildMqttMessage("GET", MerossEnum.Namespace.SYSTEM_ALL.value(),
                Collections.emptyMap());
        return MerossMqttConnector.publishMqttMessage(systemAllMessage, requestTopic);
    }

    private HashSet<String> getAbilities(String deviceName) {
        initializeMerossMqttConnector();
        String requestTopic = MerossMqttConnector
                .buildDeviceRequestTopic(merossHttpConnector.getDevUUIDByDevName(deviceName));
        byte[] systemAbilityMessage = MerossMqttConnector.buildMqttMessage("GET",
                MerossEnum.Namespace.SYSTEM_ABILITY.value(), Collections.emptyMap());
        JsonElement digestElement = JsonParser
                .parseString(MerossMqttConnector.publishMqttMessage(systemAbilityMessage, requestTopic));
        String abilityString = digestElement.getAsJsonObject().get("payload").getAsJsonObject().get("ability")
                .getAsJsonObject().toString();
        TypeToken<HashMap<String, HashMap<String, String>>> type = new TypeToken<>() {
        };
        HashMap<String, HashMap<String, String>> abilities = new Gson().fromJson(abilityString, type);
        return new HashSet<>(abilities.keySet());
    }
}
