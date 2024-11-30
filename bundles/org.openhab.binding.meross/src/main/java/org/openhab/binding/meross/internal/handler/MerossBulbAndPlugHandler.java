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
import static org.openhab.binding.meross.internal.handler.MerossBridgeHandler.getHttpConnector;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

public class MerossBulbAndPlugHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MerossBulbAndPlugHandler.class);
    private final MerossManager manager = new MerossManager(getHttpConnector());
    private @Nullable MerossBulbAndPlugConfiguration config;
    private @Nullable ScheduledFuture<?> updateStateSchedule;
    private final ScheduledExecutorService localScheduler = Executors.newSingleThreadScheduledExecutor();

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
            boolean deviceExists = getHttpConnector().deviceExistsByName(config.deviceName);
            int onlineStatus = manager.onlineStatus(config.deviceName);
            if (!deviceExists) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device does not not exist");
            } else if (onlineStatus != MerossEnum.OnlineStatus.ONLINE.value()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device offline");
            } else
                updateStatus(ThingStatus.ONLINE);
            updateStateSchedule = localScheduler.scheduleWithFixedDelay(this::updateChannelState, 1, 1,
                    TimeUnit.SECONDS);

        });
    }

    private void updateChannelState() {
        int onOffStatus = manager.togglexOnOffStatus(config.deviceName);
        if (onOffStatus == MerossEnum.OnOffStatus.OFF.value())
            updateState(CHANNEL_TOGGLEX, StringType.valueOf("Off"));
        else
            updateState(CHANNEL_TOGGLEX, StringType.valueOf("On"));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_TOGGLEX)) {
            handleTogglexChannel(channelUID, command);
        } else {
            logger.debug("Unsupported channelUID {}", channelUID);
        }
    }

    private void handleTogglexChannel(ChannelUID channelUID, Command command) {
        logger.info("Channel togglex");
        if (command instanceof StringType) {
            switch (command.toString()) {
                case "ON" -> {
                    logger.info("Toggled On");
                    manager.togglexOn(config.deviceName);
                }
                case "OFF" -> {
                    logger.info("Toggled Off");
                    manager.togglexOff(config.deviceName);
                }
            }
        } else {
            logger.debug("Unsupported command {} for channel {}", command, channelUID);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> sfupdate = updateStateSchedule;
        if (sfupdate != null) {
            sfupdate.cancel(true);
            updateStateSchedule = null;
        }
    }
}
