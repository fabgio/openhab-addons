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
package org.openhab.binding.meross.internal.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossHttpConnector;
import org.openhab.binding.meross.internal.api.MerossMqttConnector;
import org.openhab.binding.meross.internal.command.Command;
import org.openhab.binding.meross.internal.factory.ModeFactory;
import org.openhab.binding.meross.internal.factory.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossManager} abstract class is responsible for implementing general functionalities to interact with
 * appliances
 *
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public abstract class MerossManager {
    private static final Logger logger = LoggerFactory.getLogger(MerossManager.class);
    private final MerossHttpConnector merossHttpConnector;

    MerossManager(MerossHttpConnector merossHttpConnector) {
        this.merossHttpConnector = merossHttpConnector;
    }

    void initializeMqttConnector() {
        String clientId = Objects.requireNonNull(MerossMqttConnector.buildClientId());
        MerossMqttConnector.setClientId(clientId);
        String userId = Objects.requireNonNull(merossHttpConnector.getCredentials().userId());
        MerossMqttConnector.setUserId(userId);
        String key = Objects.requireNonNull(merossHttpConnector.getCredentials().key());
        MerossMqttConnector.setKey(key);
        String brokerAddress = Objects.requireNonNull(merossHttpConnector.getCredentials().mqttDomain());
        MerossMqttConnector.setBrokerAddress(brokerAddress);
    }

    /**
     * @param deviceName The device name
     * @param commandType The command type
     * @param commandMode The command Mode
     * @return The MQTT response
     */

    public void executeCommand(String deviceName, String commandType, String commandMode) {
        initializeMqttConnector();
        String deviceUUID = Objects.requireNonNull(merossHttpConnector.getDevUUIDByDevName(deviceName));
        String requestTopic = MerossMqttConnector.buildDeviceRequestTopic(deviceUUID);
        ModeFactory modeFactory = TypeFactory.getFactory(commandType);
        Command command = modeFactory.commandMode(commandMode);
        byte[] commandMessage = command.commandType(commandType);
        if (!abilities(deviceName).contains(MerossEnum.Namespace.getAbilityValueByName(commandType))) {
            logger.warn("Command {} not supported", commandType);
        }
        MerossMqttConnector.publishMqttMessage(commandMessage, requestTopic);
    }

    public int online(String deviceName) {
        String systemAllPublishesMessage = systemAll(deviceName);
        JsonElement jsonElement = JsonParser.parseString(systemAllPublishesMessage);
        JsonObject jsonObject = jsonElement.getAsJsonObject().getAsJsonObject().get("payload").getAsJsonObject()
                .get("all").getAsJsonObject().get("system").getAsJsonObject().get("online").getAsJsonObject();
        return jsonObject.get("status").getAsInt();
    }

    String systemAll(String deviceName) {
        initializeMqttConnector();
        String requestTopic = MerossMqttConnector
                .buildDeviceRequestTopic(merossHttpConnector.getDevUUIDByDevName(deviceName));
        byte[] systemAllMessage = MerossMqttConnector.buildMqttMessage("GET", MerossEnum.Namespace.SYSTEM_ALL.value(),
                Collections.emptyMap());
        return MerossMqttConnector.publishMqttMessage(systemAllMessage, requestTopic);
    }

    HashSet<String> abilities(String deviceName) {
        initializeMqttConnector();
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
