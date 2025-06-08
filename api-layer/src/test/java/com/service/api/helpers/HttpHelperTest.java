package com.service.api.helpers;

import org.junit.jupiter.api.Test;

import com.service.api.helpers.Constants;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.HttpURLConnection;

class HttpHelperTest {
    @Test
    void constructorIsPrivate() throws Exception {
        Constructor<HttpHelper> constructor = HttpHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    void buildHttpURLConnectionReturnsNullOnInvalidUrl() {
        assertThrows(IllegalArgumentException.class, () -> {
            HttpHelper.buildHttpURLConnection("invalid-url", "GET", "application/json", false, "appId");
        });
    }

    @Test
    void buildHttpURLConnectionValidUrl() {
        HttpURLConnection connection = HttpHelper.buildHttpURLConnection(
                "https://example.com", "GET", "application/json", false, "appId");
        assertNotNull(connection);
        assertEquals("GET", connection.getRequestMethod());
        assertEquals("application/json", connection.getRequestProperty(Constants.CONTENT_TYPE));
    }
}

