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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meross.internal.api.MerossHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * The {@link MerossBulbAndPlugManager} is the concrete implementation of MerossManager for bulbs and plugs
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossBulbAndPlugManager extends MerossManager {
    private final Logger logger = LoggerFactory.getLogger(MerossBulbAndPlugManager.class);

    public MerossBulbAndPlugManager(MerossHttpConnector merossHttpConnector) {
        super(merossHttpConnector);
    }

    private JsonArray togglexArray(String deviceName) {
        JsonElement jsonElement = JsonParser.parseString(systemAll(deviceName));
        return jsonElement.getAsJsonObject().getAsJsonObject().get("payload").getAsJsonObject().get("all")
                .getAsJsonObject().get("digest").getAsJsonObject().get("togglex").getAsJsonArray();
    }

    public int channel(String deviceName) {
        return togglexArray(deviceName).get(0).getAsJsonObject().get("channel").getAsInt();
    }

    public int onoff(String deviceName) {
        return togglexArray(deviceName).get(0).getAsJsonObject().get("onoff").getAsInt();
    }

    public long lmt(String deviceName) {
        return togglexArray(deviceName).get(0).getAsJsonObject().get("lmTime").getAsLong();
    }
}
