package com.enterprise.agents.common.util;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.exception.OAuthException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class OAuthUtils {
    public static String buildAuthorizationUrl(OAuthConfig config, String state) {
        return UriComponentsBuilder.fromHttpUrl(config.getAuthorizationUrl())
                .queryParam("client_id", config.getClientId())
                .queryParam("redirect_uri", config.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", config.getScopes()))
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public static HttpEntity<MultiValueMap<String, String>> buildTokenRequest(
            OAuthConfig config, String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", config.getRedirectUri());
        body.add("grant_type", "authorization_code");

        return new HttpEntity<>(body, headers);
    }

    public static HttpEntity<MultiValueMap<String, String>> buildRefreshTokenRequest(
            OAuthConfig config, String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", config.getClientId());
        body.add("client_secret", config.getClientSecret());
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        return new HttpEntity<>(body, headers);
    }

    public static String buildOAuthUrl(String authUrl, String clientId, String redirectUri, String scope, String state) {
        try {
            return String.format("%s?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                    authUrl, clientId, redirectUri, scope, state);
        } catch (Exception e) {
            throw new OAuthException("Failed to build OAuth URL", e);
        }
    }

    public static String extractEnterpriseId(String state) {
        try {
            return new String(Base64Utils.decodeFromString(state));
        } catch (Exception e) {
            throw new OAuthException("Failed to extract enterprise ID from state", e);
        }
    }

    public static MultiValueMap<String, String> buildTokenRequest(String code, String clientId, String clientSecret, String redirectUri) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("code", code);
        request.add("client_id", clientId);
        request.add("client_secret", clientSecret);
        request.add("redirect_uri", redirectUri);
        request.add("grant_type", "authorization_code");
        return request;
    }
} 