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
package org.openhab.binding.meross.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Meross API exception
 *
 * @author Giovanni Fabiani - Initial contribution
 *
 */
@NonNullByDefault
public class MerossException extends Exception {
    public MerossException(String message) {
        super(message);
    }

    public MerossException(String message, Throwable cause) {
        super(message, cause);
    }
}
