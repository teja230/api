package com.enterprise.agents.jira.service;

import com.enterprise.agents.common.model.Company;
import com.enterprise.agents.common.model.IntegrationConfiguration;
import com.enterprise.agents.common.model.IntegrationType;
import com.enterprise.agents.common.repository.IntegrationConfigurationRepository;
import com.enterprise.agents.common.service.BaseIntegrationService;
import com.enterprise.agents.common.service.TokenManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class JiraIntegrationService extends BaseIntegrationService {
    private final TokenManagementService tokenManagementService;

    public JiraIntegrationService(
            IntegrationConfigurationRepository configRepository,
            TokenManagementService tokenManagementService,
            @Value("${app.base-url}") String baseUrl) {
        super(configRepository, baseUrl);
        this.tokenManagementService = tokenManagementService;
    }

    @Override
    protected String exchangeCodeForToken(IntegrationConfiguration config, String code) {
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        org.springframework.util.LinkedMultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", baseRedirectUri + "/api/jira/oauth/callback");

        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> request =
                new org.springframework.http.HttpEntity<>(params, headers);
        java.util.Map<String, Object> response = restTemplate.postForObject(
                config.getType().getTokenUrl(), request, java.util.Map.class);

        if (response == null || response.get("access_token") == null) {
            throw new RuntimeException("Failed to exchange code for JIRA token");
        }
        return (String) response.get("access_token");
    }

    @Override
    public void storeToken(String token, IntegrationConfiguration config) {
        tokenManagementService.storeToken(
                config.getCompany().getId(),
                IntegrationType.JIRA,
                token,
                null, // refresh token
                "Bearer", // token type
                LocalDateTime.now().plusHours(1), // expires in 1 hour
                config.getScopes()
        );
    }

    @Override
    public IntegrationConfiguration configure(Long companyId, IntegrationType type, String clientId, String clientSecret) {
        Company company = new Company();
        company.setId(companyId);

        IntegrationConfiguration config = new IntegrationConfiguration();
        config.setCompany(company);
        config.setType(type);
        config.setClientId(clientId);
        config.setClientSecret(clientSecret);
        return configRepository.save(config);
    }
} 