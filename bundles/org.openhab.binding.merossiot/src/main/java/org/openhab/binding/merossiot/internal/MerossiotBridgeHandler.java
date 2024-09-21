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
package org.openhab.binding.merossiot.internal;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.meross4j.communication.MerossEnum;
import org.meross4j.communication.MerossHttpConnector;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossiotBridgeHandler} is responsible for handling  http communication with Meross Host.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossiotBridgeHandler extends BaseBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(MerossiotBindingConstants.THING_TYPE_BRIDGE);
    private final Logger logger = LoggerFactory.getLogger(MerossiotBridgeHandler.class);
    private final String apiBaseUrl;
    private final String email;
    private final String password;
    final MerossHttpConnector merossHttpConnector;

    public MerossiotBridgeHandler(Bridge bridge) {
        super(bridge);
        MerossiotConfiguration config = getConfigAs(MerossiotConfiguration.class);
        apiBaseUrl = config.apibaseUrl;
        email = config.email;
        password = config.password;
        merossHttpConnector =  new MerossHttpConnector(apiBaseUrl, email, password);;
    }

    @Override
    public void initialize() {
        if (apiBaseUrl.isBlank() || email.isBlank() || password.isBlank()) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@test/offline,configuration-error");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            int statusCode = merossHttpConnector.login().statusCode();
            int errorCode = merossHttpConnector.getErrorCode();
            String errorMessage = MerossEnum.ErrorCode.getMessageByStatusCode(errorCode);
            if (statusCode != 200) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error");
                logger.warn("Communication resulted in status code {}",statusCode);
            } else if (errorCode != MerossEnum.ErrorCode.NOT_AN_ERROR.getValue()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/" + errorMessage);
                logger.warn("Communication resulted in error code {} with message {}",errorCode,errorMessage);
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
