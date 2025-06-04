package com.service.api.helpers;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}

