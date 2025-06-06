package com.enterprise.agents.slack.controller;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.common.model.ApiResponse;
import com.enterprise.agents.common.util.OAuthUtils;
import com.enterprise.agents.slack.model.SlackOAuthToken;
import com.enterprise.agents.slack.service.SlackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/slack")
@RequiredArgsConstructor
public class SlackOAuthController {
    private final OAuthConfig oauthConfig;
    private final RestTemplate restTemplate;
    private final SlackService slackService;

    @GetMapping("/oauth/url")
    public ResponseEntity<ApiResponse<String>> getOAuthUrl(@RequestParam String enterpriseId) {
        String state = enterpriseId; // Using enterpriseId as state for simplicity
        String url = OAuthUtils.buildAuthorizationUrl(oauthConfig, state);
        return ResponseEntity.ok(ApiResponse.success(url));
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleCallback(
            @RequestParam String code,
            @RequestParam String state) {
        try {
            var request = OAuthUtils.buildTokenRequest(oauthConfig, code);
            var response = restTemplate.postForEntity(
                    oauthConfig.getTokenUrl(),
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();
                SlackOAuthToken token = new SlackOAuthToken();
                token.setEnterpriseId(state);
                token.setAccessToken((String) tokenData.get("access_token"));
                token.setBotAccessToken((String) tokenData.get("bot_access_token"));
                token.setTeamId((String) tokenData.get("team_id"));
                token.setTeamName((String) tokenData.get("team_name"));
                token.setBotUserId((String) tokenData.get("bot_user_id"));
                token.setScope((String) tokenData.get("scope"));

                slackService.saveToken(token);
                return ResponseEntity.ok(ApiResponse.success(tokenData));
            }

            throw new OAuthException("invalid_response", "Failed to exchange code for token");
        } catch (Exception e) {
            throw new OAuthException("token_exchange_failed", e.getMessage(), e);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkStatus(@RequestParam String enterpriseId) {
        boolean isConnected = slackService.isConnected(enterpriseId);
        return ResponseEntity.ok(ApiResponse.success(isConnected));
    }
} 