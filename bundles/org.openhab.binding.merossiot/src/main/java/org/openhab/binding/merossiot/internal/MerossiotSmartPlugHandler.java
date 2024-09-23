package org.openhab.binding.merossiot.internal;

import static org.openhab.binding.merossiot.internal.MerossiotBindingConstants.*;

import java.util.Set;

import org.meross4j.communication.MerossEnum;
import org.meross4j.communication.MerossManager;
import org.meross4j.record.response.Response;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MerossiotSmartPlugHandler} is responsible for communicating with MerossIOT smartPlug
 *
 * @author Giovanni Fabiani
 */
public class MerossiotSmartPlugHandler extends MerossiotBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SMART_PLUG);
    private final Logger logger = LoggerFactory.getLogger(MerossiotSmartPlugHandler.class);
    private final Thing thing = getThing();

    public MerossiotSmartPlugHandler(Thing thing) {
        super((Bridge) thing);
    }

    /**
     * Handles SmartPlugs ON/OFF commands
     *
     * @param channelUID The channel
     * @param command The command
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug(
                    "The bridge has not been initialized yet. Can not process command for channel {} with command {}.",
                    channelUID.getAsString(), command.toFullString());
            return;
        }
        String label = thing.getLabel();
        MerossManager merossManager = MerossManager.createMerossManager(merossHttpConnector);
        if (channelUID.getId().equals(CHANNEL_TOGGLEX)) {
            handleToggleXChannel(command, merossManager, label);
        } else {
            logger.debug("The channel {} is not supported.", channelUID.getAsString());
        }
    }

    /**
     * @param command The command to handle
     * @param merossManager The MerossManager
     * @param label The label of the command
     */
    private void handleToggleXChannel(Command command, MerossManager merossManager, String label) {
        if (command instanceof RefreshType) {
            Response response = merossManager.executeCommand(label, MerossEnum.Namespace.CONTROL_TOGGLEX.getValue());
            Integer deviceStatus = (Integer) response.map().get("onoff");
            if (deviceStatus != null) {
                if (deviceStatus == 0) {
                    updateState(CHANNEL_TOGGLEX, OnOffType.OFF);
                } else {
                    updateState(CHANNEL_TOGGLEX, OnOffType.ON);
                }
            } else {
                logger.debug("Cannot retrieve the device's status.");
            }
        } else if (command instanceof OnOffType) {
            if (OnOffType.ON.equals(command)) {
                merossManager.executeCommand(label, MerossEnum.Namespace.CONTROL_TOGGLEX.getValue(),
                        OnOffType.ON.name());
                updateState(CHANNEL_TOGGLEX, OnOffType.ON);
            } else if (OnOffType.OFF.equals(command)) {
                merossManager.executeCommand(label, MerossEnum.Namespace.CONTROL_TOGGLEX.getValue(),
                        OnOffType.OFF.name());
                updateState(CHANNEL_TOGGLEX, OnOffType.OFF);
            }
        } else {
            logger.debug("The command {} is not supported.", command.toFullString());
        }
    }
}
