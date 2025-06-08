package com.enterprise.agents.github.service;

import com.enterprise.agents.common.model.IntegrationConfiguration;
import com.enterprise.agents.common.repository.IntegrationConfigurationRepository;
import com.enterprise.agents.common.service.IntegrationLoggingService;
import com.enterprise.agents.common.service.TokenManagementService;
import com.enterprise.agents.common.service.TokenRefreshService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class GitHubTokenRefreshService extends TokenRefreshService {
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TokenManagementService tokenManagementService;
    private final IntegrationConfigurationRepository configRepository;
    private final IntegrationLoggingService loggingService;

    public GitHubTokenRefreshService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            TokenManagementService tokenManagementService,
            IntegrationConfigurationRepository configRepository,
            IntegrationLoggingService loggingService) {
        super(tokenManagementService, configRepository, loggingService);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenManagementService = tokenManagementService;
        this.configRepository = configRepository;
        this.loggingService = loggingService;
    }

    @Override
    protected String refreshTokenForType(IntegrationConfiguration config, String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(config.getClientId(), config.getClientSecret());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        String response = restTemplate.postForObject(GITHUB_TOKEN_URL, request, String.class);
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse GitHub token refresh response", e);
        }
    }
} 