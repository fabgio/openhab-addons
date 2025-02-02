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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossBulbAndPlugHandler} class is responsible for handling http communication with plugs and bulbs
 *
 * @author Giovanni Fabiani - Initial contribution
 */

public class MerossBulbAndPlugHandler extends MerossThingHandler {
    private static final long REFRESH_INTERVAL = 500; // milliseconds
    private final Logger logger = LoggerFactory.getLogger(MerossBulbAndPlugHandler.class);

    public MerossBulbAndPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        scheduler.scheduleAtFixedRate(this::updateChannelStateAsync, 0, REFRESH_INTERVAL, TimeUnit.MILLISECONDS);
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
        if (command instanceof StringType) {
            switch (command.toString()) {
                case "ON" ->
                    manager.executeCommand(config.deviceName, MerossEnum.Namespace.CONTROL_TOGGLEX.name(), "ON");
                case "OFF" ->
                    manager.executeCommand(config.deviceName, MerossEnum.Namespace.CONTROL_TOGGLEX.name(), "OFF");
            }
        } else {
            logger.debug("Unsupported command {} for channel {}", command, channelUID);
        }
    }

    private void updateChannelState(int onOffStatus) {
        if (onOffStatus == MerossEnum.OnOffStatus.OFF.value()) {
            updateState(CHANNEL_TOGGLEX, new StringType("Off"));
        } else if (onOffStatus == MerossEnum.OnOffStatus.ON.value()) {
            updateState(CHANNEL_TOGGLEX, new StringType("On"));
        }
    }

    private void updateChannelStateAsync() {
        CompletableFuture.supplyAsync(() -> manager.togglexOnOffStatus(config.deviceName))
                .thenAccept(this::updateChannelState).join();
    }
}
