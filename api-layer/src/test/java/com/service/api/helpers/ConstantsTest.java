package com.service.api.helpers;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ConstantsTest {
    @Test
    void constantsValuesAreCorrect() {
        assertEquals("Content-Type", Constants.CONTENT_TYPE);
        assertEquals(3, Constants.MAX_RETRY_SERVICE_REQUEST);
        assertEquals(300000, Constants.REST_CLIENT_TIMEOUT_MS);
        assertEquals("HUBSPOTSERVICE", Constants.API_SERVICE);
        assertEquals("application/json", Constants.APPLICATION_JSON);
        assertEquals("POST", Constants.POST_REQUEST_METHOD);
        assertEquals("GET", Constants.GET_REQUEST_METHOD);
        assertEquals("HS_1652", Constants.LogCodes.INFO_1652);
        assertEquals("HS_1654", Constants.LogCodes.INFO_1654);
    }

    @Test
    void constantsConstructorIsPrivate() throws Exception {
        Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    void logCodesConstructorIsPrivate() throws Exception {
        Constructor<Constants.LogCodes> constructor = Constants.LogCodes.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }
}

