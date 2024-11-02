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

import com.google.gson.annotations.SerializedName;

/**
 * @param token The token issued by the Meross cloud
 * @param key The key issued by the Meross cloud
 * @param userId The userid
 * @param userEmail the user's email
 * @param domain The Meross cloud http domain
 * @param mqttDomain he Meross cloud http domain
 *
 * @author Giovanni Fabiani - Initial contribution
 */
public record CloudCredentials(@SerializedName(value = "token") String token, @SerializedName(value = "key") String key,
        @SerializedName(value = "userid") String userId, // mqtt
        @SerializedName(value = "email") String userEmail, @SerializedName(value = "domain") String domain,
        @SerializedName(value = "mqttDomain") String mqttDomain// mqtt
) {
}
