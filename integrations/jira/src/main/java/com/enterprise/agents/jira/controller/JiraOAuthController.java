package com.enterprise.agents.jira.controller;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.common.model.ApiResponse;
import com.enterprise.agents.common.util.OAuthUtils;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.service.JiraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/jira")
@RequiredArgsConstructor
public class JiraOAuthController {
    private final OAuthConfig oauthConfig;
    private final RestTemplate restTemplate;
    private final JiraService jiraService;

    @GetMapping("/oauth/url")
    public ResponseEntity<ApiResponse<String>> getOAuthUrl(@RequestParam String enterpriseId) {
        String state = enterpriseId;
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
                JiraOAuthToken token = new JiraOAuthToken();
                token.setEnterpriseId(state);
                token.setAccessToken((String) tokenData.get("access_token"));
                token.setRefreshToken((String) tokenData.get("refresh_token"));
                token.setScope((String) tokenData.get("scope"));
                token.setSiteUrl((String) tokenData.get("site_url"));
                token.setAccountId((String) tokenData.get("account_id"));
                token.setDisplayName((String) tokenData.get("display_name"));
                token.setEmail((String) tokenData.get("email"));

                jiraService.saveToken(token);
                return ResponseEntity.ok(ApiResponse.success(tokenData));
            }

            throw new OAuthException("invalid_response", "Failed to exchange code for token");
        } catch (Exception e) {
            throw new OAuthException("token_exchange_failed", e.getMessage(), e);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkStatus(@RequestParam String enterpriseId) {
        boolean isConnected = jiraService.isConnected(enterpriseId);
        return ResponseEntity.ok(ApiResponse.success(isConnected));
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjects(@RequestParam String enterpriseId) {
        var projects = jiraService.getProjects(enterpriseId);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }
} 