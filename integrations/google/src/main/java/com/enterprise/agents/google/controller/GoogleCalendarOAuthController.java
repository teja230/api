package com.enterprise.agents.google.controller;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.common.model.ApiResponse;
import com.enterprise.agents.common.util.OAuthUtils;
import com.enterprise.agents.google.model.GoogleCalendarOAuthToken;
import com.enterprise.agents.google.service.GoogleCalendarService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/google")
@RequiredArgsConstructor
public class GoogleCalendarOAuthController {
    private final @Qualifier("googleConfig") OAuthConfig oauthConfig;
    private final RestTemplate restTemplate;
    private final GoogleCalendarService calendarService;

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
                GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
                token.setEnterpriseId(state);
                token.setAccessToken((String) tokenData.get("access_token"));
                token.setRefreshToken((String) tokenData.get("refresh_token"));
                token.setScope((String) tokenData.get("scope"));
                token.setTokenType((String) tokenData.get("token_type"));

                // Fetch user info
                var userInfo = calendarService.getUserInfo(token.getAccessToken());
                token.setEmail((String) userInfo.get("email"));
                token.setName((String) userInfo.get("name"));
                token.setPicture((String) userInfo.get("picture"));
                token.setTimezone((String) userInfo.get("timezone"));

                calendarService.saveToken(token);
                return ResponseEntity.ok(ApiResponse.success(tokenData));
            }

            throw new OAuthException("invalid_response", "Failed to exchange code for token");
        } catch (Exception e) {
            throw new OAuthException("token_exchange_failed", e.getMessage(), e);
        }
    }

    @GetMapping("/calendar/status")
    public ResponseEntity<ApiResponse<Boolean>> checkStatus(
            @RequestParam String enterpriseId,
            HttpSession session) {
        // Verify SSO session
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated via SSO"));
        }

        boolean isConnected = calendarService.isConnected(enterpriseId);
        return ResponseEntity.ok(ApiResponse.success(isConnected));
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEvents(@RequestParam String enterpriseId) {
        var events = calendarService.getEvents(enterpriseId);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
} 