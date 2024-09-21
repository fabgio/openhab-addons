package org.openhab.binding.merossiot.internal;

import java.util.Set;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;

/**
 * {@link MerossiotSmartPlugHandler} is responsible for communicating with  Merossiot smartPlug
 *
 * @author Giovanni Fabiani
 */
public class MerossiotSmartPlugHandler extends MerossiotBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set
            .of(MerossiotBindingConstants.THING_TYPE_SMART_PLUG);


    public MerossiotSmartPlugHandler(Thing thing) {
        super((Bridge) thing);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
