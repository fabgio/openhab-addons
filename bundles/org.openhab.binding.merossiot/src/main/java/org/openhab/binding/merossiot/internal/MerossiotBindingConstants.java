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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link MerossiotBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossiotBindingConstants {

    private static final String BINDING_ID = "merossiot";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MEROSSIOT = new ThingTypeUID(BINDING_ID, "merossiot");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream.of(
            new ThingTypeUID(BINDING_ID,"smartPlug")).collect(Collectors.toSet()));
    // List of all Channel ids
    public static final String TOGGLEX = "togglex";
}
