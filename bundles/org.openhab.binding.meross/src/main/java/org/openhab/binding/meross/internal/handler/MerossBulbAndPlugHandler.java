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

import static org.openhab.binding.meross.internal.MerossBindingConstants.CHANNEL_TOGGLEX;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.config.MerossBulbAndPlugConfiguration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossBulbAndPlugHandler} is responsible for handling http communication with plugs and bulbs
 *
 * @author Giovanni Fabiani - Initial contribution
 */

@NonNullByDefault
public class MerossBulbAndPlugHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MerossBulbAndPlugHandler.class);
    private @Nullable MerossBulbAndPlugConfiguration config;

    public MerossBulbAndPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof MerossBridgeHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not set");
            return;
        }
        config = getConfigAs(MerossBulbAndPlugConfiguration.class);
        scheduler.execute(() -> {
            int deviceStatus = MerossBridgeHandler.sConnector.getDevStatusByDevName(config.deviceName);
            if (deviceStatus != MerossEnum.OnlineStatus.ONLINE.getValue()) {
                updateStatus(ThingStatus.OFFLINE);
                logger.info("Device is offline with code: {} reason: {} ", deviceStatus,
                        MerossEnum.OnlineStatus.OFFLINE.name());
            } else {
                updateStatus(ThingStatus.ONLINE);
                logger.info("Device is online with code: {} reason:  {}", deviceStatus,
                        MerossEnum.OnlineStatus.ONLINE.name());
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_TOGGLEX)) {
            logger.info("channel TOGGLEX");
            if (command instanceof StringType) {
                if (command.toString().equals("ON")) {
                    logger.info("Toggled On");
                    MerossManager.createMerossManager(MerossBridgeHandler.sConnector).executeCommand(config.deviceName,
                            MerossEnum.Namespace.CONTROL_TOGGLEX.name(), "ON");
                } else if (command.toString().equals("OFF")) {
                    logger.info("Toggled Off");
                    MerossManager.createMerossManager(MerossBridgeHandler.sConnector).executeCommand(config.deviceName,
                            MerossEnum.Namespace.CONTROL_TOGGLEX.name(), "OFF");
                }
            } else {
                logger.debug("Unsupported command {} for channel {}", command, channelUID);
            }
        } else {
            logger.debug("Unsupported channelUID {}", channelUID);
        }
    }
}
