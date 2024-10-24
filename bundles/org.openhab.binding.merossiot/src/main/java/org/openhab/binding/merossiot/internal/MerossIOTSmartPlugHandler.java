package org.openhab.binding.merossiot.internal;

import static org.openhab.binding.merossiot.internal.MerossIOTBindingConstants.CHANNEL_TOGGLEX;

import org.meross4j.communication.MerossEnum;
import org.meross4j.communication.MerossHttpConnector;
import org.meross4j.communication.MerossManager;
import org.meross4j.record.response.Response;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MerossIOTSmartPlugHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MerossIOTSmartPlugHandler.class);
    private final Thing thing = getThing();
    MerossHttpConnector merossHttpConnector;

    public MerossIOTSmartPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("The bridge has not been initialized yet");
        } else {
            String label = thing.getLabel();
            if (merossHttpConnector != null) {
                int status = merossHttpConnector.getDevStatusByDevName(label);
                if (status == 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.communication-error");
                    logger.warn("Communication resulted in status code {}", status);
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                logger.debug("MerossHttpConnector is not initialized yet");
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (merossHttpConnector != null) {
            MerossManager merossManager = MerossManager.createMerossManager(merossHttpConnector);
            String label = thing.getLabel();
            if (channelUID.getId().equals(CHANNEL_TOGGLEX)) {
                handleToggleXChannel(command, merossManager, label);
            } else {
                logger.debug("The channel {} is not supported.", channelUID.getAsString());
            }
        } else {
            logger.debug("MerossHttpConnector is not initialized.");
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
