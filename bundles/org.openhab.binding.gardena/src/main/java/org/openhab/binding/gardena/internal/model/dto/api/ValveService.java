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
package org.openhab.binding.gardena.internal.model.dto.api;

import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.library.unit.Units;

/**
 * Represents a Gardena object that is sent via the Gardena API.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class ValveService {
    public UserDefinedNameWrapper name;
    public TimestampedStringValue activity;
    public TimestampedStringValue state;
    public TimestampedStringValue lastErrorCode;
    public TimestampedIntegerValue duration;
    public @NonNull Unit<@NonNull Time> durationUnit = Units.SECOND;
}
