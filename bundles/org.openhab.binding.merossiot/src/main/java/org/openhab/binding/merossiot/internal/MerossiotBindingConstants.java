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
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MerossiotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossiotBindingConstants {

    private static final String BINDING_ID = "MerossIOT";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SMART_PLUG = new ThingTypeUID(BINDING_ID, "smartPlug");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE, THING_TYPE_SMART_PLUG);
    // List of all Channel ids
    public static final String CHANNEL_TOGGLEX = "togglex";
}
