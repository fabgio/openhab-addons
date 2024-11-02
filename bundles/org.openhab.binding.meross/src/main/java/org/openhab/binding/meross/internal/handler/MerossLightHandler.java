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
package org.openhab.binding.meross.internal.handler;

import static org.openhab.binding.meross.internal.MerossBindingConstants.CHANNEL_TOGGLEX;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.config.MerossLightConfiguration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossLightHandler} class is responsible for handling http communication with plugs and bulbs
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossLightHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MerossLightHandler.class);
    private MerossLightConfiguration config = new MerossLightConfiguration();
    final @Nullable MerossManager manager = new MerossManager(MerossBridgeHandler.connector);

    public MerossLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null) {
            return;
        }
        config = getConfigAs(MerossLightConfiguration.class);
        logger.info("Initializing MerossLightHandler {}", config);
        var connector = MerossBridgeHandler.connector;

        initializeBridge(bridge.getStatus());
        String uuid = connector.getDevUUIDByDevName(config.lightName);
        logger.info("checking for uuid....   {}", uuid);
        if (uuid == null) {
            logger.warn("No device found with name {}", config.lightName);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "No device found with name " + config.lightName);
            return;
        }

        var status = manager.onlineStatus(config.lightName);
        if (status == null) {
            logger.warn("Online status is null");
            return;
        }
        logger.info("OnlineStatus: {}", status);
        initializeLight(status.getAsInt());
        initializeBridge(bridge.getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null) {
            initializeBridge(bridgeStatusInfo.getStatus());
        }
    }

    public void initializeBridge(ThingStatus bridgeStatus) {
        if (bridgeStatus == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public void initializeLight(int lightStatus) {
        if (lightStatus == MerossEnum.OnlineStatus.UNKNOWN.value()
                || lightStatus == MerossEnum.OnlineStatus.NOT_ONLINE.value()
                || lightStatus == MerossEnum.OnlineStatus.UPGRADING.value()) {
            updateStatus(ThingStatus.UNKNOWN);
        } else if (lightStatus == MerossEnum.OnlineStatus.OFFLINE.value()) {
            updateStatus(ThingStatus.OFFLINE);
        } else if (lightStatus == MerossEnum.OnlineStatus.ONLINE.value()) {
            updateStatus(ThingStatus.ONLINE);
            logger.info("Light {} {}", config.lightName, MerossEnum.OnlineStatus.ONLINE.name());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (thing.getStatus() == ThingStatus.OFFLINE) {
            return;
        }
        if (channelUID.getId().equals(CHANNEL_TOGGLEX)) {
            if (command instanceof StringType) {
                switch (command.toString()) {
                    case "ON" ->
                        manager.executeCommand(config.lightName, MerossEnum.Namespace.CONTROL_TOGGLEX.name(), "ON");
                    case "OFF" ->
                        manager.executeCommand(config.lightName, MerossEnum.Namespace.CONTROL_TOGGLEX.name(), "OFF");
                }
            } else {
                logger.debug("Unsupported command {} for channel {}", command, channelUID);
            }
        } else {
            logger.debug("Unsupported channelUID {}", channelUID);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
