package com.enterprise.agents.jira.service;

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
public class JiraTokenRefreshService extends TokenRefreshService {
    private static final String JIRA_TOKEN_URL = "https://auth.atlassian.com/oauth/token";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public JiraTokenRefreshService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            TokenManagementService tokenManagementService,
            IntegrationConfigurationRepository configRepository,
            IntegrationLoggingService loggingService) {
        super(tokenManagementService, configRepository, loggingService);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String refreshTokenForType(IntegrationConfiguration config, String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        String response = restTemplate.postForObject(JIRA_TOKEN_URL, request, String.class);
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("error")) {
                throw new RuntimeException("JIRA token refresh failed: " + jsonNode.get("error").asText());
            }
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JIRA token refresh response", e);
        }
    }
} 