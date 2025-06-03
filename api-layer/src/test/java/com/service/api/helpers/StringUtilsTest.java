package com.service.api.helpers;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {
    @Test
    void constructorIsPrivate() throws Exception {
        Constructor<StringUtils> constructor = StringUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof IllegalStateException);
    }

    @Test
    void toStringReturnsStringFromInputStream() throws IOException {
        String input = "hello world";
        ByteArrayInputStream stream = new ByteArrayInputStream(input.getBytes());
        assertEquals(input, StringUtils.toString(stream));
    }

    @Test
    void toStringReturnsEmptyStringForNullStream() throws IOException {
        assertEquals("", StringUtils.toString(null));
    }

    @Test
    void trimDoubleQuotesRemovesQuotes() {
        assertEquals("abc", StringUtils.trimDoubleQuotes("\"abc\""));
    }

    @Test
    void trimDoubleQuotesReturnsOriginalIfNoQuotes() {
        assertEquals("abc", StringUtils.trimDoubleQuotes("abc"));
    }
}

