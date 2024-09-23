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

import static org.openhab.binding.merossiot.internal.MerossiotBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link MerossiotThingHandlerFactory} is responsible for creating Meross things and MerossIOT thing
 * handlers.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.merossiot", service = ThingHandlerFactory.class)
public class MerossiotThingHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (MerossiotBridgeHandler.SUPPORTED_THING_TYPES.equals(thingTypeUID)) {
            return new MerossiotBridgeHandler((Bridge) thing);
        } else if (MerossiotSmartPlugHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            new MerossiotSmartPlugHandler(thing);
        }

        return null;
    }
}
