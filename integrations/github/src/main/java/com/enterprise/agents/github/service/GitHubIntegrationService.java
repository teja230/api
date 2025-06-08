package com.enterprise.agents.github.service;

import com.enterprise.agents.common.model.Company;
import com.enterprise.agents.common.model.IntegrationConfiguration;
import com.enterprise.agents.common.model.IntegrationType;
import com.enterprise.agents.common.repository.IntegrationConfigurationRepository;
import com.enterprise.agents.common.service.BaseIntegrationService;
import com.enterprise.agents.common.service.TokenManagementService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class GitHubIntegrationService extends BaseIntegrationService {
    private final RestTemplate restTemplate;
    private final String tokenUrl;
    private final TokenManagementService tokenManagementService;
    private final ObjectMapper objectMapper;

    public GitHubIntegrationService(
            IntegrationConfigurationRepository configRepository,
            TokenManagementService tokenManagementService,
            @Value("${app.base-url}") String baseUrl,
            @Value("${github.oauth.token-url}") String tokenUrl) {
        super(configRepository, baseUrl);
        this.restTemplate = new RestTemplate();
        this.tokenUrl = tokenUrl;
        this.tokenManagementService = tokenManagementService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected String buildOAuthUrl(IntegrationConfiguration config, String state) {
        return String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                config.getClientId(),
                config.getRedirectUri(),
                config.getScopes(),
                state
        );
    }

    @Override
    protected String exchangeCodeForToken(IntegrationConfiguration config, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", config.getClientId());
        map.add("client_secret", config.getClientSecret());
        map.add("code", code);
        map.add("redirect_uri", config.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            String response = restTemplate.postForObject(tokenUrl, request, String.class);
            JsonNode jsonResponse = objectMapper.readTree(response);

            // Store the token
            tokenManagementService.storeToken(
                    config.getCompany().getId(),
                    IntegrationType.GITHUB,
                    jsonResponse.get("access_token").asText(),
                    jsonResponse.has("refresh_token") ? jsonResponse.get("refresh_token").asText() : null,
                    jsonResponse.get("token_type").asText(),
                    LocalDateTime.now().plus(jsonResponse.get("expires_in").asLong(), ChronoUnit.SECONDS),
                    config.getScopes()
            );

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange code for token", e);
        }
    }

    @Override
    public void handleOAuthCallback(Long companyId, IntegrationType type, String code, String state) {
        // Validate state
        // TODO: Implement state validation

        // Get configuration
        IntegrationConfiguration config = configRepository
                .findByCompany_IdAndType(companyId, type)
                .orElseThrow(() -> new RuntimeException("Integration not configured"));

        // Exchange code for token
        exchangeCodeForToken(config, code);
    }

    @Override
    public boolean isConnected(Long companyId, IntegrationType type) {
        return tokenManagementService.hasValidToken(companyId, type);
    }

    @Override
    public void disconnect(Long companyId, IntegrationType type) {
        tokenManagementService.deleteToken(companyId, type);
        super.disconnect(companyId, type);
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

    @Override
    public void storeToken(String token, IntegrationConfiguration config) {
        tokenManagementService.storeToken(
                config.getCompany().getId(),
                IntegrationType.GITHUB,
                token,
                null, // refresh token
                "Bearer", // token type
                LocalDateTime.now().plusHours(1), // expires in 1 hour
                config.getScopes()
        );
    }
} 