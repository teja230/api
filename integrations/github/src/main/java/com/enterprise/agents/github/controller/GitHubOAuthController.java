package com.enterprise.agents.github.controller;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.common.model.ApiResponse;
import com.enterprise.agents.common.util.OAuthUtils;
import com.enterprise.agents.github.model.GitHubOAuthToken;
import com.enterprise.agents.github.service.GitHubService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubOAuthController {
    private final @Qualifier("gitHubConfig") OAuthConfig oauthConfig;
    private final RestTemplate restTemplate;
    private final GitHubService githubService;

    @GetMapping("/oauth/url")
    public ResponseEntity<ApiResponse<String>> getOAuthUrl(
            @RequestParam String enterpriseId,
            HttpSession session) {
        // Check if user is authenticated via SSO
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated via SSO"));
        }

        String state = enterpriseId;
        String url = OAuthUtils.buildAuthorizationUrl(oauthConfig, state);
        return ResponseEntity.ok(ApiResponse.success(url));
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session) {
        // Verify SSO session
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated via SSO"));
        }

        try {
            var request = OAuthUtils.buildTokenRequest(oauthConfig, code);
            var response = restTemplate.postForEntity(
                    oauthConfig.getTokenUrl(),
                    request,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();
                GitHubOAuthToken token = new GitHubOAuthToken();
                token.setEnterpriseId(state);
                token.setAccessToken((String) tokenData.get("access_token"));
                token.setScope((String) tokenData.get("scope"));
                token.setTokenType((String) tokenData.get("token_type"));

                // Fetch user info
                var userInfo = githubService.getUserInfo(token.getAccessToken());
                token.setLogin((String) userInfo.get("login"));
                token.setName((String) userInfo.get("name"));
                token.setEmail((String) userInfo.get("email"));
                token.setAvatarUrl((String) userInfo.get("avatar_url"));

                githubService.saveToken(token);
                return ResponseEntity.ok(ApiResponse.success(tokenData));
            }

            throw new OAuthException("invalid_response", "Failed to exchange code for token");
        } catch (Exception e) {
            throw new OAuthException("token_exchange_failed", e.getMessage(), e);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkStatus(
            @RequestParam String enterpriseId,
            HttpSession session) {
        // Verify SSO session
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated via SSO"));
        }

        boolean isConnected = githubService.isConnected(enterpriseId);
        return ResponseEntity.ok(ApiResponse.success(isConnected));
    }

    @GetMapping("/repositories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRepositories(
            @RequestParam String enterpriseId,
            HttpSession session) {
        // Verify SSO session
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated via SSO"));
        }

        var repos = githubService.getRepositories(enterpriseId);
        return ResponseEntity.ok(ApiResponse.success(repos));
    }
} 