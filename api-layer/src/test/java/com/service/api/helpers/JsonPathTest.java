package com.service.api.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonPathTest {
    @Test
    void hasValueReturnsTrueForExistingPath() {
        JsonObject obj = JsonParser.parseString("{\"a\":{\"b\":\"c\"}} ").getAsJsonObject();
        assertTrue(JsonPath.hasValue(obj, "a.b"));
    }

    @Test
    void hasValueReturnsFalseForMissingPath() {
        JsonObject obj = JsonParser.parseString("{\"a\":{\"b\":\"c\"}} ").getAsJsonObject();
        assertFalse(JsonPath.hasValue(obj, "a.x"));
    }

    @Test
    void getValueReturnsValueForExistingPath() {
        JsonObject obj = JsonParser.parseString("{\"a\":{\"b\":\"c\"}} ").getAsJsonObject();
        assertEquals("c", JsonPath.getValue(obj, "a.b"));
    }

    @Test
    void getValueReturnsDefaultForMissingPath() {
        JsonObject obj = JsonParser.parseString("{\"a\":{\"b\":\"c\"}} ").getAsJsonObject();
        assertEquals("default", JsonPath.getValue(obj, "a.x", "default"));
    }
}

