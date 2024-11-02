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
package org.openhab.binding.meross.internal.handler;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.config.MerossThingConfiguration;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * The {@link MerossThingHandler} abstract class is responsible for handling basic communication with devices. It
 * should be implemented by concrete classes
 *
 * @author Giovanni Fabiani - Initial contribution
 */

abstract class MerossThingHandler extends BaseThingHandler {
    @Nullable
    MerossThingConfiguration config;
    final MerossManager manager = new MerossManager(MerossBridgeHandler.connector);

    public MerossThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof MerossBridgeHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not set");
            return;
        }
        config = getConfigAs(MerossThingConfiguration.class);
        int onlineStatus = manager.onlineStatus(config.deviceName);
        if (onlineStatus != MerossEnum.OnlineStatus.ONLINE.value()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device offline");
        } else if (MerossBridgeHandler.connector.getDevUUIDByDevName(config.deviceName) == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No device found with that name");
        } else {
            updateStatus(ThingStatus.ONLINE);
            MerossBridgeHandler.connector.logout();
        }
    }
}
