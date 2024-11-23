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
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.openhab.binding.meross.internal.dto.CloudCredentials;
import org.openhab.binding.meross.internal.dto.Device;
import org.openhab.binding.meross.internal.handler.MerossBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossHttpConnector} class is responsible for handling the Http functionality for connecting to the Meross
 * Cloud
 *
 * @author Giovanni Fabiani - Initial contribution
 */
public class MerossHttpConnector {
    private final static Logger logger = LoggerFactory.getLogger(MerossHttpConnector.class);
    private static final String INITIAL_STRING = "23x17ahWarFH6w29";
    private static final String DEFAULT_APP_TYPE = "MerossIOT";
    private static final String MODULE_VERSION = "0.0.0";
    private static final long CONNECTION_TIMEOUT_SECONDS = 15;
    private String token;
    private final String apiBaseUrl;
    private final String userName;
    private final String password;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(CONNECTION_TIMEOUT_SECONDS, ChronoUnit.SECONDS)).build();

    public MerossHttpConnector(String apiBaseUrl, String userName, String password) {
        this.apiBaseUrl = apiBaseUrl;
        this.userName = userName;
        this.password = password;
    }

    private HttpResponse<String> postResponse(Map<String, String> paramsData, String uri, String path)
            throws MerossException {
        String dataToSign;
        String encodedParams;
        String authorizationValue;
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        long timestamp = Instant.now().toEpochMilli();
        if (paramsData != null) {
            encodedParams = encodeParams(paramsData);
        } else {
            throw new MerossException("Parameter data map is null");
        }
        dataToSign = "%s%d%s%s".formatted(INITIAL_STRING, timestamp, nonce, encodedParams);
        String md5hash = MD5Util.getMD5String(dataToSign);
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("params", encodedParams);
        payloadMap.put("sign", md5hash);
        payloadMap.put("timestamp", String.valueOf(timestamp));
        payloadMap.put("nonce", nonce);
        String payload = new Gson().toJson(payloadMap);
        if (token != null) {
            authorizationValue = "Basic %s".formatted(token);
        } else {
            authorizationValue = "Basic";
        }
        HttpRequest postRequest = HttpRequest.newBuilder().uri(URI.create(uri + path))
                .header("Authorization", authorizationValue).header("AppVersion", "0.0.0").header("vender", "meross")
                .header("AppType", DEFAULT_APP_TYPE).header("AppLanguage", "EN")
                .header("User-Agent", DEFAULT_APP_TYPE + "/" + MODULE_VERSION)
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build();
        try {
            return client.sendAsync(postRequest, HttpResponse.BodyHandlers.ofString()).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Error while posting data", e);
            throw new MerossException("Error while posting data", e);
        }
    }

    private static String encodeParams(Map<String, String> paramsData) {
        return Base64.getEncoder().encodeToString(new Gson().toJson(paramsData).getBytes());
    }

    /**
     * @return The http response at login request
     */
    public HttpResponse<String> login() {
        try {
            Map<String, String> loginMap = Map.of("email", userName, "password", password);

            HttpResponse<String> stringHttpResponse = Objects
                    .requireNonNull(postResponse(loginMap, apiBaseUrl, MerossEnum.HttpEndpoint.LOGIN.value()));
            return stringHttpResponse;
        } catch (MerossException e) {
            logger.debug("Error while login", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the status code from the Meross API
     */
    public int apiStatus() {
        return JsonParser.parseString(login().body()).getAsJsonObject().get("apiStatus").getAsInt();
    }

    public synchronized CloudCredentials fetchCredentials() {
        if (apiStatus() != MerossEnum.ApiStatusCode.OK.value()) {
            try {
                throw new MerossException(MerossEnum.ApiStatusCode.getMessageByApiStatusCode(apiStatus())
                        + " with Error code: " + apiStatus());
            } catch (MerossException e) {
                logger.info("Exception caught while fetching credentials {}", e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            JsonElement jsonElement = JsonParser.parseString(login().body());
            String data = jsonElement.getAsJsonObject().get("data").toString();
            return new Gson().fromJson(data, CloudCredentials.class);
        }
    }

    public synchronized ArrayList<Device> fetchDevices() {
        String token = fetchCredentials().token();
        setToken(token);
        try {
            HttpResponse<String> response = Objects.requireNonNull(
                    postResponse(Collections.EMPTY_MAP, apiBaseUrl, MerossEnum.HttpEndpoint.DEV_LIST.value()));
            JsonElement jsonElement = JsonParser.parseString(response.body());
            String data = jsonElement.getAsJsonObject().get("data").toString();
            TypeToken<ArrayList<Device>> type = new TypeToken<>() {
            };
            return new Gson().fromJson(data, type);
        } catch (MerossException e) {
            logger.debug("Exception caught while fetching devices", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param devName The device name
     * @return The device UUID
     */
    public String getDevUUIDByDevName(String devName) {
        return getDevices().stream().filter(device -> device.devName().equals(devName)).map(Device::uuid).findFirst()
                .orElseThrow(() -> new RuntimeException("No device found with name: " + devName));
    }

    private void setToken(String token) {
        this.token = token;
    }

    private String readFile(File file) {
        String content = null;
        try {
            logger.info("Reading file '{}'.", file.getPath());
            content = Files.readString(file.toPath());
        } catch (IOException e) {
            logger.error("Couldn't read from file '{}'.", file.getPath(), e);
        }
        return content;
    }

    /**
     * @return The user's credentials
     */
    public CloudCredentials getCredentials() {
        return new Gson().fromJson(readFile(MerossBridgeHandler.credentialfile), CloudCredentials.class);
    }

    /**
     * @return The user's devices
     */
    public ArrayList<Device> getDevices() {
        TypeToken<ArrayList<Device>> type = new TypeToken<>() {
        };
        return new Gson().fromJson(readFile(MerossBridgeHandler.deviceFile), type);
    }

    public void logout() {
        try {
            Objects.requireNonNull(
                    postResponse(Collections.emptyMap(), apiBaseUrl, MerossEnum.HttpEndpoint.LOGOUT.value()));
        } catch (MerossException e) {
            logger.debug("Error while logging out", e);
            throw new RuntimeException(e);
        }
    }
}