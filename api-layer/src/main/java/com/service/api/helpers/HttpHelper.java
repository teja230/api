package com.service.api.helpers;

import com.google.common.base.Strings;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.service.api.helpers.Constants.*;
import static com.service.api.helpers.Constants.LogCodes.INFO_1652;
import static com.service.api.helpers.Constants.LogCodes.INFO_1654;

public class HttpHelper {
    private static final Logger logger = LoggerFactory.getLogger(HttpHelper.class);
    private static final Gson gson = new GsonBuilder().create();

    private HttpHelper() {
        throw new IllegalStateException("HttpHelper should be used as a utility class");
    }

    /**
     * Build HTTP URL Connection with basic information
     */
    public static HttpURLConnection buildHttpURLConnection(String url, String requestMethod, String contentType, boolean doOutput, String appId) {
        HttpURLConnection httpURLConnection = null;
        int connectionTimeout = REST_CLIENT_TIMEOUT_MS;
        try {
            httpURLConnection = createHttpUrlConnection(url);
            httpURLConnection.setDoOutput(doOutput);
            httpURLConnection.setRequestMethod(requestMethod);
            httpURLConnection.setRequestProperty(CONTENT_TYPE, contentType);
            httpURLConnection.setConnectTimeout(connectionTimeout);
            httpURLConnection.setReadTimeout(connectionTimeout);
        } catch (Exception exception) {
            logger.error("[{}][{}][{}] Exception in buildHttpURLConnection", appId, API_SERVICE, INFO_1652, exception);
            throw new IllegalArgumentException("Invalid URL: " + url, exception);
        }
        return httpURLConnection;
    }

    /**
     * Send HTTP POST request (supports, application/json)
     */
    public static JsonElement sendHttpRequest(String requestQuery, HttpURLConnection httpURLConnection, String appId) throws IOException {
        JsonElement jsonResponse = new JsonObject();
        int connectionTimeout = REST_CLIENT_TIMEOUT_MS;
        // Implement retry for request timeout.
        int count = 0;
        if (httpURLConnection != null) {
            while (count < MAX_RETRY_SERVICE_REQUEST) {
                count++;
                try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                    if (requestQuery != null) {
                        outputStream.write(requestQuery.getBytes());
                    }
                    return processHttpResponse(httpURLConnection, appId);
                } catch (SocketTimeoutException ex) {
                    if (count >= MAX_RETRY_SERVICE_REQUEST) {
                        throw new ServiceRuntimeException(INFO_1654, ex, count, connectionTimeout, ex.getMessage());
                    } else {
                        logger.warn("[{}][{}][{}] Timeout on attempt {} of {} with timeout {} ms", appId, API_SERVICE, INFO_1652, count, MAX_RETRY_SERVICE_REQUEST, connectionTimeout);
                    }
                }
            }
        } else {
            logger.warn("[{}][{}][{}] HTTPURLConnection not a valid value in sendHttpRequest()", appId, API_SERVICE, INFO_1652);
        }
        return jsonResponse;
    }

    /**
     * Send HTTP GET request
     */
    public static JsonElement sendHttpRequest(HttpURLConnection httpURLConnection, String appId) throws IOException {
        JsonElement jsonResponse = new JsonObject();
        int connectionTimeout = REST_CLIENT_TIMEOUT_MS;
        // Check for null HttpURLConnection
        if (httpURLConnection == null) {
            logger.warn("[{}][{}][{}] HTTPURLConnection not a valid value in sendHttpRequest()", appId, API_SERVICE, INFO_1652);
            return jsonResponse;
        }
        // Implement retry for request timeout.
        int count = 0;
        while (count < MAX_RETRY_SERVICE_REQUEST) {
            count++;
            try (InputStream ignored = httpURLConnection.getInputStream()) {
                return processHttpResponse(httpURLConnection, appId);
            } catch (SocketTimeoutException ex) {
                if (count >= MAX_RETRY_SERVICE_REQUEST) {
                    throw new ServiceRuntimeException(INFO_1654, ex, count, connectionTimeout, ex.getMessage());
                } else {
                    logger.warn("[{}][{}][{}] Timeout on attempt {} of {} with timeout {} ms", appId, API_SERVICE, INFO_1652, count, MAX_RETRY_SERVICE_REQUEST, connectionTimeout);
                }
            }
        }
        return jsonResponse;
    }

    /**
     * Process HTTP Response
     */
    private static JsonElement processHttpResponse(HttpURLConnection httpURLConnection, String appId) throws IOException {
        JsonElement jsonResponse = new JsonObject();
        int httpResponseCode = httpURLConnection.getResponseCode();

        if (httpResponseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
            String errorStream = StringUtils.toString(httpURLConnection.getErrorStream());
            logger.info("[{}][{}][{}] Server returned error response: {} with code {}", appId, API_SERVICE, INFO_1652, errorStream, httpResponseCode);
            throw new ServiceRuntimeException(INFO_1654, httpResponseCode, errorStream);
        }
        String response = StringUtils.toString(httpURLConnection.getInputStream());
        if (!Strings.isNullOrEmpty(response)) {
            return gson.fromJson(response, JsonElement.class);
        }
        return jsonResponse;
    }

    public static void extractHttpErrors(StringBuilder apiError, ServiceRuntimeException ex) throws IOException {
        if (ex.getMessageArguments() != null && ex.getMessageArguments().length > 1) {
            String errorResponse = ex.getMessageArguments()[1].toString();
            if (!Strings.isNullOrEmpty(errorResponse)) {
                JsonReader jsonErrorReader = new JsonReader(new StringReader(errorResponse));
                jsonErrorReader.setLenient(true);
                while (jsonErrorReader.hasNext()) {
                    JsonToken nextToken = jsonErrorReader.peek();
                    if (JsonToken.STRING.equals(nextToken)) {
                        apiError.append(jsonErrorReader.nextString()).append(" ");
                    } else if (JsonToken.BEGIN_OBJECT.equals(nextToken) || JsonToken.BEGIN_ARRAY.equals(nextToken)) {
                        processErrorJson(apiError, errorResponse);
                        return;
                    } else if (JsonToken.END_DOCUMENT.equals(nextToken)) {
                        break;
                    }
                }
            }
        }
    }

    private static void processErrorJson(StringBuilder apiError, String errorResponse) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();

            JsonElement jsonElement = gson.fromJson(errorResponse, JsonElement.class);
            JsonObject jsonObject;
            if (jsonElement != null) {
                if (jsonElement.isJsonArray()) {
                    jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
                    Map<String, Object> errorMap = gson.fromJson(jsonObject.toString(), HashMap.class);
                    errorMap.forEach((key, value) -> apiError.append(key).append(" - ").append(value).append("  "));
                } else {
                    jsonObject = jsonElement.getAsJsonObject();
                    Map<String, Object> errorMap = gson.fromJson(jsonObject.toString(), HashMap.class);
                    errorMap.forEach((key, value) -> apiError.append(key).append(" - ").append(value).append("  "));
                }
            }
        } catch (JsonSyntaxException ex) {
            apiError.append(errorResponse);
        }
    }

    private static HttpURLConnection createHttpUrlConnection(String url) throws IOException {
        try {
            URI requestUri = new URI(url);
            return (HttpURLConnection) requestUri.toURL().openConnection();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL syntax: " + url, e);
        }
    }

    /**
     * Send HTTP request with a simpler interface
     */
    public static String sendHttpRequest(String url, String payload) throws IOException {
        HttpURLConnection connection = buildHttpURLConnection(url, "POST", "application/json", true, "default");
        if (connection == null) {
            throw new IOException("Failed to create HTTP connection");
        }
        JsonElement response = sendHttpRequest(payload, connection, "default");
        return response.toString();
    }
}
