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
package org.openhab.binding.meross.internal.api;

import java.util.stream.Stream;

/**
 * The {@link MerossEnum} class is responsible for defining constants for the whole binding
 * handlers.
 *
 * @author Giovanni Fabiani - Initial contribution
 */

public class MerossEnum {
    public enum HttpEndpoint {
        LOGIN("/v1/Auth/signIn"),
        LOGOUT("/v1/Profile/logout"),
        DEV_LIST("/v1/Device/devList");

        public String getValue() {
            return value;
        }

        private final String value;

        HttpEndpoint(String value) {
            this.value = value;
        }
    }

    public enum ErrorCode {
        OK(0),
        WRONG_OR_MISSING_USER(1000),
        WRONG_OR_MISSING_PASSWORD(1001),
        ACCOUNT_DOES_NOT_EXIST(1002),
        THIS_ACCOUNT_HAS_BEEN_DISABLED_OR_DELETED(1003),
        WRONG_EMAIL_OR_PASSWORD(1004),
        INVALID_EMAIL_ADDRESS(1005),
        BAD_PASSWORD_FORMAT(1006),
        USER_ALREADY_EXISTS(1007),
        THIS_EMAIL_IS_NOT_REGISTERED(1008),
        SEND_EMAIL_FAILED(1009),
        WRONG_TICKET(1011);
        // TODO:TO BE COMPLETED

        public int getValue() {
            return value;
        }

        private final int value;

        ErrorCode(int value) {
            this.value = value;
        }

        public static String getMessageByErrorCode(int statusCode) {
            return Stream.of(ErrorCode.values()).filter(s -> s.getValue() == statusCode).map(ErrorCode::name)
                    .findFirst().orElse("Unidentified Http Error Message");
        }
    }
}
