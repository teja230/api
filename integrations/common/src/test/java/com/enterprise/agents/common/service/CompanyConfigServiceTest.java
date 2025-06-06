package com.enterprise.agents.common.service;

import com.enterprise.agents.common.config.MinimalTestApplication;
import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.common.model.CompanyConfig;
import com.enterprise.agents.common.repository.CompanyConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = MinimalTestApplication.class
)
@ContextConfiguration(classes = MinimalTestApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
class CompanyConfigServiceTest {

    @Autowired
    private CompanyConfigService configService;

    @Autowired
    private CompanyConfigRepository configRepository;

    @BeforeEach
    void setUp() {
        configRepository.deleteAll();
    }

    @Test
    void testSaveAndGetConfig() {
        // Given
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId("test-company");
        config.setCompanyName("Test Company");
        config.setPrimaryColor("#FF0000");
        config.setSecondaryColor("#00FF00");
        config.setEnableSlackNotifications(true);
        config.setEnableJiraIntegration(true);

        // When
        configService.saveConfig(config);
        Optional<CompanyConfig> retrieved = configService.getConfig("test-company");

        // Then
        assertTrue(retrieved.isPresent());
        assertEquals("Test Company", retrieved.get().getCompanyName());
        assertEquals("#FF0000", retrieved.get().getPrimaryColor());
        assertTrue(retrieved.get().getEnableSlackNotifications());
    }

    @Test
    void testUpdateSetting() {
        // Given
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId("test-company");
        config.setCompanyName("Test Company");
        configService.saveConfig(config);

        // When
        configService.updateSetting("test-company", "custom.setting", "value");

        // Then
        Optional<CompanyConfig> retrieved = configService.getConfig("test-company");
        assertTrue(retrieved.isPresent());
        assertEquals("value", retrieved.get().getSettings().get("custom.setting"));
    }

    @Test
    void testIsIntegrationEnabled() {
        // Given
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId("test-company");
        config.setCompanyName("Test Company");
        config.setEnableSlackNotifications(true);
        config.setEnableJiraIntegration(false);
        configService.saveConfig(config);

        // Then
        assertTrue(configService.isIntegrationEnabled("test-company", "slack"));
        assertFalse(configService.isIntegrationEnabled("test-company", "jira"));
        assertFalse(configService.isIntegrationEnabled("test-company", "nonexistent"));
    }

    @Test
    void testDeleteConfig() {
        // Given
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId("test-company");
        config.setCompanyName("Test Company");
        configService.saveConfig(config);

        // When
        configService.deleteConfig("test-company");

        // Then
        assertFalse(configService.getConfig("test-company").isPresent());
    }
}
