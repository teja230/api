package com.enterprise.agents.common.service;

import com.enterprise.agents.common.model.CompanyConfig;
import com.enterprise.agents.common.repository.CompanyConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CompanyConfigService {
    private final CompanyConfigRepository configRepository;

    // Inject self-reference for proper cacheable method invocation
    @Autowired
    @Lazy
    private CompanyConfigService self;

    public CompanyConfigService(CompanyConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Cacheable(value = "companyConfigs", key = "#companyId")
    public Optional<CompanyConfig> getConfig(String companyId) {
        return configRepository.findByCompanyId(companyId);
    }

    @Transactional
    @CacheEvict(value = "companyConfigs", key = "#config.companyId")
    public CompanyConfig saveConfig(CompanyConfig config) {
        return configRepository.save(config);
    }

    @Transactional
    @CacheEvict(value = "companyConfigs", key = "#companyId")
    public void updateSetting(String companyId, String key, String value) {
        configRepository.findByCompanyId(companyId).ifPresent(config -> {
            config.getSettings().put(key, value);
            configRepository.save(config);
        });
        // Call cacheable method via self-reference
        self.getConfig(companyId);
    }

    @Transactional
    @CacheEvict(value = "companyConfigs", key = "#companyId")
    public void deleteConfig(String companyId) {
        configRepository.findByCompanyId(companyId).ifPresent(configRepository::delete);
    }

    public boolean isIntegrationEnabled(String companyId, String integration) {
        return getConfig(companyId)
                .map(config -> {
                    switch (integration.toLowerCase()) {
                        case "slack":
                            return config.getEnableSlackNotifications();
                        case "jira":
                            return config.getEnableJiraIntegration();
                        case "github":
                            return config.getEnableGitHubIntegration();
                        case "google":
                            return config.getEnableGoogleCalendarIntegration();
                        default:
                            return false;
                    }
                })
                .orElse(false);
    }
}
