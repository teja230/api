package com.enterprise.agents.common.service;

import com.enterprise.agents.common.model.IntegrationConfiguration;
import com.enterprise.agents.common.model.IntegrationType;
import com.enterprise.agents.common.repository.IntegrationConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public abstract class BaseIntegrationService implements IntegrationService {
    protected final IntegrationConfigurationRepository configRepository;
    protected final String baseRedirectUri;

    protected BaseIntegrationService(
            IntegrationConfigurationRepository configRepository,
            String baseRedirectUri) {
        this.configRepository = configRepository;
        this.baseRedirectUri = baseRedirectUri;
    }

    @Override
    public String generateOAuthUrl(Long companyId, IntegrationType type, String state) {
        IntegrationConfiguration config = configRepository.findByCompany_IdAndType(companyId, type)
                .orElseThrow(() -> new RuntimeException("Integration not configured"));

        return buildOAuthUrl(config, state);
    }

    protected String buildOAuthUrl(IntegrationConfiguration config, String state) {
        return UriComponentsBuilder.fromHttpUrl(config.getType().getOAuthUrl())
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", baseRedirectUri + "/api/" + config.getType().name().toLowerCase() + "/oauth/callback")
                .queryParam("response_type", "code")
                .queryParam("scope", config.getScopes())
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    @Transactional
    public void handleOAuthCallback(Long companyId, IntegrationType type, String code, String state) {
        // Validate state (implemented in controller)
        IntegrationConfiguration config = configRepository.findByCompany_IdAndType(companyId, type)
                .orElseThrow(() -> new RuntimeException("Integration not configured"));

        // Exchange code for token
        String token = exchangeCodeForToken(config, code);

        // Store token (implemented in specific service)
        storeToken(token, config);
    }

    protected abstract String exchangeCodeForToken(IntegrationConfiguration config, String code);

    protected abstract void storeToken(String token, IntegrationConfiguration config);

    @Override
    public boolean isConnected(Long companyId, IntegrationType type) {
        return configRepository.existsByCompany_IdAndType(companyId, type);
    }

    @Override
    @Transactional
    public void disconnect(Long companyId, IntegrationType type) {
        configRepository.deleteByCompany_IdAndType(companyId, type);
    }
} 