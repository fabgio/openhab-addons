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
package org.openhab.binding.sonyprojector.internal.communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sonyprojector.internal.SonyProjectorException;
import org.openhab.core.types.StateOption;
import org.openhab.core.util.HexUtils;

/**
 * Represents the different mosquito noise reduction modes available for the projector
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public enum SonyProjectorMosquitoNr {

    HIGH("High", new byte[] { 0x00, 0x03 }),
    MIDDLE("Middle", new byte[] { 0x00, 0x02 }),
    LOW("Low", new byte[] { 0x00, 0x01 }),
    OFF("Off", new byte[] { 0x00, 0x00 });

    private String name;
    private byte[] dataCode;

    /**
     * Constructor
     *
     * @param name the name of the mosquito noise reduction mode
     * @param dataCode the data code identifying the mosquito noise reduction mode
     */
    private SonyProjectorMosquitoNr(String name, byte[] dataCode) {
        this.name = name;
        this.dataCode = dataCode;
    }

    /**
     * Get the data code identifying the current mosquito noise reduction mode
     *
     * @return the data code
     */
    public byte[] getDataCode() {
        return dataCode;
    }

    /**
     * Get the name of the current mosquito noise reduction mode
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of {@link StateOption} associated to the available mosquito noise reduction modes
     *
     * @return the list of {@link StateOption} associated to the available mosquito noise reduction modes
     */
    public static List<StateOption> getStateOptions() {
        List<StateOption> options = new ArrayList<>();
        for (SonyProjectorMosquitoNr value : SonyProjectorMosquitoNr.values()) {
            options.add(new StateOption(value.getName(), value.getName()));
        }
        return options;
    }

    /**
     * Get the mosquito noise reduction mode associated to a name
     *
     * @param name the name used to identify the mosquito noise reduction mode
     *
     * @return the mosquito noise reduction mode associated to the searched name
     *
     * @throws SonyProjectorException - If no mosquito noise reduction mode is associated to the searched name
     */
    public static SonyProjectorMosquitoNr getFromName(String name) throws SonyProjectorException {
        for (SonyProjectorMosquitoNr value : SonyProjectorMosquitoNr.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        throw new SonyProjectorException("Invalid name for a mosquito noise reduction mode: " + name);
    }

    /**
     * Get the mosquito noise reduction mode associated to a data code
     *
     * @param dataCode the data code used to identify the mosquito noise reduction mode
     *
     * @return the mosquito noise reduction mode associated to the searched data code
     *
     * @throws SonyProjectorException - If no mosquito noise reduction mode is associated to the searched data code
     */
    public static SonyProjectorMosquitoNr getFromDataCode(byte[] dataCode) throws SonyProjectorException {
        for (SonyProjectorMosquitoNr value : SonyProjectorMosquitoNr.values()) {
            if (Arrays.equals(dataCode, value.getDataCode())) {
                return value;
            }
        }
        throw new SonyProjectorException(
                "Invalid data code for a mosquito noise reduction mode: " + HexUtils.bytesToHex(dataCode));
    }
}
