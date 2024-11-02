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

import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossHttpConnector;
import org.openhab.binding.meross.internal.api.MerossHttpConnectorBuilder;
import org.openhab.binding.meross.internal.config.MerossBridgeConfiguration;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossBridgeHandler} is responsible for handling http communication with Meross Host.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(MerossBridgeHandler.class);
    private @Nullable MerossBridgeConfiguration config;
    static @Nullable MerossHttpConnector connector;
    private static final String CREDENTIAL_FILE_NAME = "meross" + File.separator + "meross_credentials.json";
    public static final String DEVICE_FILE_NAME = "meross" + File.separator + "meross_devices.json";
    public static final File credentialfile = new File(
            OpenHAB.getUserDataFolder() + File.separator + CREDENTIAL_FILE_NAME);
    public static final File deviceFile = new File(OpenHAB.getUserDataFolder() + File.separator + DEVICE_FILE_NAME);

    public MerossBridgeHandler(Thing thing) {
        super((Bridge) thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(MerossBridgeConfiguration.class);
        scheduler.execute(() -> {
            connector = MerossHttpConnectorBuilder.newBuilder().setApiBaseUrl(config.hostname)
                    .setUserName(config.username).setPassword(config.password).setCredentialFile(credentialfile)
                    .setDeviceFile(deviceFile).build();
            int httpStatusCode = connector.login().statusCode();
            connector.logout();
            int apiStatusCode = connector.apiStatus();
            connector.logout();
            String apiMessage = MerossEnum.ApiStatusCode.getMessageByApiStatusCode(apiStatusCode);
            if (httpStatusCode != 200) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Communication error");
                logger.warn("Communication resulted in status code {}", httpStatusCode);
            } else if (apiStatusCode != MerossEnum.ApiStatusCode.OK.value()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, apiMessage);
                logger.warn("Communication resulted in error code {} with message {}", apiStatusCode, apiMessage);
            } else {
                CompletableFuture.runAsync(() -> connector.fetchCredentialsAndWrite(credentialfile))
                        .thenRunAsync(() -> connector.fetchDevicesAndWrite(deviceFile)).join();
                connector.logout();
                updateStatus(ThingStatus.ONLINE);
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
