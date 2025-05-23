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
package org.openhab.binding.networkupstools.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NUTConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class NUTConfiguration {

    /**
     * the refresh interval which is used to poll values from the NetworkUpsTools server.
     */
    public int refresh = 60;
    public String device = "";
    public String host = "localhost";
    public String username = "";
    public String password = "";
    public int port = 3493;
}
