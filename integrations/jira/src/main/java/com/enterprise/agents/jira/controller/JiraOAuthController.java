package com.enterprise.agents.jira.controller;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.common.model.ApiResponse;
import com.enterprise.agents.common.util.OAuthUtils;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.service.JiraService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jira")
@RequiredArgsConstructor
public class JiraOAuthController {

    private final JiraService jiraService;
    private final OAuthConfig oAuthConfig;

    @GetMapping("/oauth/url")
    public ResponseEntity<ApiResponse> getOAuthUrl(@RequestParam String enterpriseId, HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated via SSO"));
        }

        try {
            String url = OAuthUtils.buildOAuthUrl(
                    oAuthConfig.getJiraAuthUrl(),
                    oAuthConfig.getJiraClientId(),
                    oAuthConfig.getJiraRedirectUri(),
                    oAuthConfig.getJiraScope(),
                    enterpriseId
            );
            return ResponseEntity.ok(ApiResponse.success(url));
        } catch (OAuthException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/oauth/callback")
    public ResponseEntity<ApiResponse> handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated via SSO"));
        }

        try {
            String enterpriseId = OAuthUtils.extractEnterpriseId(state);
            JiraOAuthToken token = jiraService.exchangeCodeForToken(code, enterpriseId);

            // Get user info from Jira
            Map<String, Object> userInfo = jiraService.getUserInfo(token.getAccessToken());

            // Update token with user info
            token.setEmail((String) userInfo.get("email"));
            token.setName((String) userInfo.get("displayName"));
            token.setAccountId((String) userInfo.get("accountId"));

            jiraService.saveToken(token);

            return ResponseEntity.ok(ApiResponse.success(token));
        } catch (OAuthException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse> checkStatus(@RequestParam String enterpriseId, HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated via SSO"));
        }

        try {
            boolean connected = jiraService.isConnected(enterpriseId);
            Map<String, Object> data = new HashMap<>();
            data.put("connected", connected);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (OAuthException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<ApiResponse> disconnect(@RequestParam String enterpriseId, HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated via SSO"));
        }

        try {
            jiraService.disconnect(enterpriseId);
            return ResponseEntity.ok(ApiResponse.success("Disconnected successfully"));
        } catch (OAuthException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 