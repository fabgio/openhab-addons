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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.openhab.binding.meross.internal.dto.CloudCredentials;
import org.openhab.binding.meross.internal.dto.Device;
import org.openhab.binding.meross.internal.exceptions.MerossException;
import org.openhab.binding.meross.internal.utils.MD5Utils;
import org.openhab.core.OpenHAB;
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
    private final String email;
    private final String password;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(CONNECTION_TIMEOUT_SECONDS, ChronoUnit.SECONDS)).build();
    private static final String CREDENTIAL_FILE_NAME = "meross" + File.separator + "meross_credentials.json";
    private static final String DEVICE_FILE_NAME = "meross" + File.separator + "meross_credentials.json";

    public MerossHttpConnector(String apiBaseUrl, String email, String password) {
        this.apiBaseUrl = apiBaseUrl;
        this.email = email;
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
        String md5hash = MD5Utils.getMD5String(dataToSign);
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

    public HttpResponse<String> login() {
        try {
            Map<String, String> loginMap = Map.of("email", email, "password", password);
            return Objects.requireNonNull(postResponse(loginMap, apiBaseUrl, MerossEnum.HttpEndpoint.LOGIN.getValue()));
        } catch (MerossException e) {
            logger.debug("Error while login", e);
            throw new RuntimeException(e);
        }
    }

    public int errorCode() {
        return JsonParser.parseString(login().body()).getAsJsonObject().get("apiStatus").getAsInt();
    }

    private CloudCredentials fetchCredentialsImpl() {
        if (errorCode() != MerossEnum.ErrorCode.OK.getValue()) {
            try {
                throw new MerossException(
                        errorCode() + "with message: " + MerossEnum.ErrorCode.getMessageByErrorCode(errorCode()));
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

    private ArrayList<Device> fetchDevicesImpl() {
        String token = fetchCredentialsImpl().token();
        setToken(token);
        try {
            HttpResponse<String> response = Objects.requireNonNull(
                    postResponse(Collections.EMPTY_MAP, apiBaseUrl, MerossEnum.HttpEndpoint.DEV_LIST.getValue()));
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
        return fetchDevicesImpl().stream().filter(device -> device.devName().equals(devName)).map(Device::uuid)
                .findFirst().orElseThrow(() -> new RuntimeException("No device found with name: " + devName));
    }

    /**
     * @param devName The device name
     * @return The device's status
     */
    public int getDevStatusByDevName(String devName) {
        return fetchDevicesImpl().stream().filter(device -> device.devName().equals(devName)).map(Device::onlineStatus)
                .findFirst().orElseThrow(() -> new RuntimeException("No device found with name: " + devName));
    }

    private void setToken(String token) {
        this.token = token;
    }

    private void writeFile(String content, File file) {
        file.getParentFile().mkdirs();
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            logger.debug("Created file '{}' with content '{}'", file.getAbsolutePath(), content);
        } catch (FileNotFoundException e) {
            logger.error("Couldn't create file '{}'.", file.getPath(), e);
        } catch (IOException e) {
            logger.error("Couldn't write to file '{}'.", file.getPath(), e);
        }
    }

    private String readFile(File file) {
        String content = null;
        try {
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
        File file = new File(OpenHAB.getUserDataFolder() + File.separator + CREDENTIAL_FILE_NAME);
        CloudCredentials credentials;
        if (!file.exists()) {
            credentials = CompletableFuture.supplyAsync(this::fetchCredentialsImpl).join();
            String json = new Gson().toJson(credentials);
            writeFile(json, file);
        } else {
            credentials = new Gson().fromJson(readFile(file), CloudCredentials.class);
        }
        return credentials;
    }

    /**
     * @return The devices
     */
    public ArrayList<Device> getDevices() {
        File file = new File(OpenHAB.getUserDataFolder() + File.separator + DEVICE_FILE_NAME);
        ArrayList<Device> devices;
        TypeToken<ArrayList<Device>> type = new TypeToken<>() {
        };
        if (!file.exists()) {
            devices = CompletableFuture.supplyAsync(this::fetchDevicesImpl).join();
            String json = new Gson().toJson(devices);
            writeFile(json, file);
        } else {
            devices = new Gson().fromJson(readFile(file), type);
        }
        return devices;
    }
}
