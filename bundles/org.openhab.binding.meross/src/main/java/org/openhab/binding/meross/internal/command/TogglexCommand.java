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
package org.openhab.binding.meross.internal.command;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.dto.MQTT;

/**
 * The {@link TogglexCommand} claas is responsible for he concrete implementation of togglex command types
 *
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class TogglexCommand {
    public static class turnOn implements Command {
        @Override
        public byte[] commandType(String type) {
            Map<String, Object> payload = Map.of("togglex", Map.of("onoff", 1, "channel", 0));
            return MQTT.buildMqttMessage("SET", MerossEnum.Namespace.CONTROL_TOGGLEX.value(), payload);
        }
    }

    public static class turnOff implements Command {
        @Override
        public byte[] commandType(String type) {
            Map<String, Object> payload = Map.of("togglex", Map.of("onoff", 0, "channel", 0));
            return MQTT.buildMqttMessage("SET", MerossEnum.Namespace.CONTROL_TOGGLEX.value(), payload);
        }
    }
}
