package com.enterprise.agents.slack.service;

import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.slack.model.SlackOAuthToken;
import com.enterprise.agents.slack.repository.SlackOAuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlackService {
    private final SlackOAuthTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    @Value("${slack.oauth.client-id}")
    private String clientId;

    @Value("${slack.oauth.client-secret}")
    private String clientSecret;

    @Value("${slack.oauth.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://slack.com/api/oauth.v2.access";
    private static final String CHANNELS_URL = "https://slack.com/api/conversations.list";
    private static final String POST_MESSAGE_URL = "https://slack.com/api/chat.postMessage";
    private static final String USERS_URL = "https://slack.com/api/users.list";

    public void saveToken(SlackOAuthToken token) {
        tokenRepository.save(token);
    }

    public boolean isConnected(String enterpriseId) {
        return tokenRepository.findByEnterpriseId(enterpriseId).isPresent();
    }

    public List<String> getChannels(String enterpriseId) {
        SlackOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "Slack not connected"));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token.getAccessToken());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    CHANNELS_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());
            if (!json.optBoolean("ok")) {
                throw new OAuthException("api_error", json.optString("error"));
            }
            JSONArray channels = json.optJSONArray("channels");
            List<String> result = new ArrayList<>();
            if (channels != null) {
                for (int i = 0; i < channels.length(); i++) {
                    result.add(channels.getJSONObject(i).optString("id"));
                }
            }
            return result;
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public boolean sendMessage(String enterpriseId, String channelId, String message) {
        SlackOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "Slack not connected"));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token.getAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject body = new JSONObject();
            body.put("channel", channelId);
            body.put("text", message);

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    POST_MESSAGE_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());
            if (!json.optBoolean("ok")) {
                throw new OAuthException("api_error", json.optString("error"));
            }
            return true;
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public SlackOAuthToken exchangeCodeForToken(String code, String enterpriseId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());
            if (!json.optBoolean("ok")) {
                throw new OAuthException("api_error", json.optString("error"));
            }

            SlackOAuthToken token = new SlackOAuthToken();
            token.setEnterpriseId(enterpriseId);
            token.setAccessToken(json.optString("access_token"));
            token.setRefreshToken(json.optString("refresh_token"));
            token.setTokenType(json.optString("token_type"));
            token.setScope(json.optString("scope"));

            JSONObject team = json.optJSONObject("team");
            if (team != null) {
                token.setTeamId(team.optString("id"));
                token.setTeamName(team.optString("name"));
            }
            token.setBotUserId(json.optString("bot_user_id"));
            token.setBotAccessToken(json.optString("access_token"));

            tokenRepository.save(token);
            return token;
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public SlackOAuthToken refreshToken(String enterpriseId) {
        SlackOAuthToken existing = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "Slack not connected"));

        if (existing.getRefreshToken() == null || existing.getRefreshToken().isEmpty()) {
            throw new OAuthException("no_refresh_token", "Refresh token not available");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", existing.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());
            if (!json.optBoolean("ok")) {
                throw new OAuthException("api_error", json.optString("error"));
            }

            existing.setAccessToken(json.optString("access_token", existing.getAccessToken()));
            existing.setRefreshToken(json.optString("refresh_token", existing.getRefreshToken()));
            existing.setScope(json.optString("scope", existing.getScope()));
            existing.setTokenType(json.optString("token_type", existing.getTokenType()));

            tokenRepository.save(existing);
            return existing;
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public List<String> getUsers(String enterpriseId) {
        SlackOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "Slack not connected"));
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token.getAccessToken());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    USERS_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JSONObject json = new JSONObject(response.getBody());
            if (!json.optBoolean("ok")) {
                throw new OAuthException("api_error", json.optString("error"));
            }
            JSONArray members = json.optJSONArray("members");
            List<String> result = new ArrayList<>();
            if (members != null) {
                for (int i = 0; i < members.length(); i++) {
                    result.add(members.getJSONObject(i).optString("id"));
                }
            }
            return result;
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }
}
