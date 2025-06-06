package com.enterprise.agents.jira.controller;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.model.ApiResponse;
import com.enterprise.agents.common.util.OAuthUtils;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.service.JiraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
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
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description) {

        Map<String, Object> response = new HashMap<>();

        if (error != null) {
            response.put("status", "error");
            response.put("error", error);
            response.put("error_description", error_description);
            return ResponseEntity.ok(ApiResponse.success(response));
        }

        JiraOAuthToken token = jiraService.exchangeCodeForToken(code, state);
        response.put("status", "success");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestParam String enterpriseId) {
        Map<String, String> response = new HashMap<>();
        JiraOAuthToken token = jiraService.refreshToken(enterpriseId);

        if (token != null) {
            response.put("status", "success");
        } else {
            response.put("status", "error");
        }

        return ResponseEntity.ok(response);
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