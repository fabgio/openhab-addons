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
package org.openhab.binding.meross.internal.dto;

import java.util.Map;

/**
 * The {@link MQTTResponse} class contains the definition of the MQTT response
 *
 * @author Giovanni Fabiani - Initial contribution
 */
public record MQTTResponse(Map<String, Object> map) {
}
