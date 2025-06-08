package com.enterprise.agents.google.service;

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
public class GoogleTokenRefreshService extends TokenRefreshService {
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleTokenRefreshService(
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

        String response = restTemplate.postForObject(GOOGLE_TOKEN_URL, request, String.class);
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("error")) {
                throw new RuntimeException("Google token refresh failed: " + jsonNode.get("error").asText());
            }
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Google token refresh response", e);
        }
    }
} 