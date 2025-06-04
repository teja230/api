package com.service.api.helpers;

public final class Constants {
    public static final String CONTENT_TYPE = "Content-Type";
    public static final int MAX_RETRY_SERVICE_REQUEST = 3;
    public static final int REST_CLIENT_TIMEOUT_MS = 300000;

    public static final String API_SERVICE = "HUBSPOTSERVICE";
    public static final String APPLICATION_JSON = "application/json";
    public static final String POST_REQUEST_METHOD = "POST";
    public static final String GET_REQUEST_METHOD = "GET";

    private Constants() {
        throw new IllegalStateException("HubspotConstants should be used as a utility class");
    }

    public static final class LogCodes {
        public static final String INFO_1652 = "HS_1652";
        public static final String INFO_1654 = "HS_1654";

        private LogCodes() {
            throw new IllegalStateException("LogCodes should be used as a utility class");
        }
    }
}

