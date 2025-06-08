package com.service.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = com.enterprise.agents.ApiLayerApplication.class)
@ActiveProfiles("test")
class ApplicationTests {
    @Test
    void contextLoads() {
        // Test if the Spring application context loads successfully
    }
}
