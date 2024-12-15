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

package org.openhab.binding.meross.internal.api;

import java.io.File;

/**
 * The {@link MerossHttpConnectorBuilder} class is a builder for MerossHttpConnector
 *
 * @author Giovanni Fabiani - Initial contribution
 */
public class MerossHttpConnectorBuilder {
    private String apiBaseUrl;
    private String userName;
    private String password;
    private File credentialFile;
    private File deviceFile;

    private MerossHttpConnectorBuilder() {
    }

    public static MerossHttpConnectorBuilder newBuilder() {
        return new MerossHttpConnectorBuilder();
    }

    public MerossHttpConnectorBuilder setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
        return this;
    }

    public MerossHttpConnectorBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public MerossHttpConnectorBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public MerossHttpConnectorBuilder setCredentialFile(File credentialFile) {
        this.credentialFile = credentialFile;
        return this;
    }

    public MerossHttpConnectorBuilder setDeviceFile(File deviceFile) {
        this.deviceFile = deviceFile;
        return this;
    }

    public MerossHttpConnector build() {
        return new MerossHttpConnector(apiBaseUrl, userName, password, credentialFile, deviceFile);
    }
}
