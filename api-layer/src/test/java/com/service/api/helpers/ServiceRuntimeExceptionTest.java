package com.service.api.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceRuntimeExceptionTest {
    @Test
    void constructorWithThrowableSetsFields() {
        ServiceRuntimeException ex = new ServiceRuntimeException("CODE_%s", new RuntimeException("fail"), "ARG");
        assertEquals("CODE_ARG", ex.getMessage());
        assertEquals("CODE_%s", ex.getMessageCode());
        assertArrayEquals(new Object[]{"ARG"}, ex.getMessageArguments());
    }

    @Test
    void constructorWithoutThrowableSetsFields() {
        ServiceRuntimeException ex = new ServiceRuntimeException("CODE_%s", "ARG");
        assertEquals("CODE_ARG", ex.getMessage());
        assertEquals("CODE_%s", ex.getMessageCode());
        assertArrayEquals(new Object[]{"ARG"}, ex.getMessageArguments());
    }
}

