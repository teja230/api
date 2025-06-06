package com.enterprise.agents.common.util;

import com.enterprise.agents.common.config.OAuthConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
} 