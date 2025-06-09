package com.enterprise.agents.jira.service;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.common.util.OAuthUtils;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.repository.JiraOAuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JiraService {
    private final OAuthConfig oAuthConfig;
    private final JiraOAuthTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    public JiraOAuthToken exchangeCodeForToken(String code, String state) {
        try {
            String enterpriseId = OAuthUtils.extractEnterpriseId(state);
            MultiValueMap<String, String> request = OAuthUtils.buildTokenRequest(
                    code,
                    oAuthConfig.getJiraClientId(),
                    oAuthConfig.getJiraClientSecret(),
                    oAuthConfig.getJiraRedirectUri()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(request, headers);
            Map<String, Object> response = restTemplate.postForObject(
                    oAuthConfig.getJiraTokenUrl(),
                    entity,
                    Map.class
            );

            if (response == null || !response.containsKey("access_token")) {
                throw new OAuthException("Failed to exchange code for token", "No access token in response");
            }

            JiraOAuthToken token = new JiraOAuthToken();
            token.setAccessToken((String) response.get("access_token"));
            token.setRefreshToken((String) response.get("refresh_token"));
            token.setExpiresIn((Integer) response.get("expires_in"));
            token.setEnterpriseId(enterpriseId);

            return saveToken(token);
        } catch (Exception e) {
            throw new OAuthException("Failed to exchange code for token", e);
        }
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            return restTemplate.getForObject(
                    oAuthConfig.getJiraApiUrl() + "/rest/api/3/myself",
                    Map.class,
                    entity
            );
        } catch (Exception e) {
            throw new OAuthException("Failed to get user info", e);
        }
    }

    public JiraOAuthToken saveToken(JiraOAuthToken token) {
        return tokenRepository.save(token);
    }

    public boolean isConnected(String enterpriseId) {
        return tokenRepository.findByEnterpriseId(enterpriseId).isPresent();
    }

    public void disconnect(String enterpriseId) {
        tokenRepository.deleteByEnterpriseId(enterpriseId);
    }

    public Map<String, Object> getProjects(String enterpriseId) {
        JiraOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "JIRA not connected"));

        try {
            String url = token.getSiteUrl() + "/rest/api/3/project";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token.getAccessToken());
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            org.json.JSONArray projects = new org.json.JSONArray(response.getBody());
            java.util.List<java.util.Map<String, Object>> projectList = new java.util.ArrayList<>();
            for (int i = 0; i < projects.length(); i++) {
                org.json.JSONObject project = projects.getJSONObject(i);
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", project.optString("id"));
                map.put("key", project.optString("key"));
                map.put("name", project.optString("name"));
                map.put("description", project.optString("description"));
                projectList.add(map);
            }
            return java.util.Map.of("projects", projectList);
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public Map<String, Object> getOnboardingIssues(String enterpriseId) {
        JiraOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "JIRA not connected"));

        try {
            String url = token.getSiteUrl() + "/rest/api/3/search";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token.getAccessToken());
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");

            String jql = "project = ONBOARDING ORDER BY created DESC";
            String requestBody = "{\"jql\": \"" + jql + "\", \"maxResults\": 50}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            org.json.JSONObject jsonResponse = new org.json.JSONObject(response.getBody());
            org.json.JSONArray issues = jsonResponse.getJSONArray("issues");
            java.util.List<java.util.Map<String, Object>> issueList = new java.util.ArrayList<>();
            for (int i = 0; i < issues.length(); i++) {
                org.json.JSONObject issue = issues.getJSONObject(i);
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", issue.getString("id"));
                map.put("key", issue.getString("key"));
                map.put("summary", issue.getJSONObject("fields").getString("summary"));
                map.put("description", issue.getJSONObject("fields").optString("description"));
                issueList.add(map);
            }
            return java.util.Map.of("issues", issueList);
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public Map<String, Object> createOnboardingIssue(String enterpriseId, String summary, String description, String issueType, List<String> labels) {
        JiraOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "JIRA not connected"));

        try {
            String url = token.getSiteUrl() + "/rest/api/3/issue";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token.getAccessToken());
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");

            org.json.JSONObject fields = new org.json.JSONObject();
            fields.put("project", new org.json.JSONObject().put("key", "ONBOARDING"));
            fields.put("summary", summary);
            fields.put("description", description);
            fields.put("issuetype", new org.json.JSONObject().put("name", issueType));
            if (labels != null && !labels.isEmpty()) {
                org.json.JSONArray labelsArray = new org.json.JSONArray(labels);
                fields.put("labels", labelsArray);
            }

            org.json.JSONObject requestBody = new org.json.JSONObject();
            requestBody.put("fields", fields);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            org.json.JSONObject createdIssue = new org.json.JSONObject(response.getBody());
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("id", createdIssue.getString("id"));
            result.put("key", createdIssue.getString("key"));
            result.put("self", createdIssue.getString("self"));
            return result;
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public Map<String, Object> getIssues(String enterpriseId) {
        JiraOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "JIRA not connected"));

        try {
            String url = token.getSiteUrl() + "/rest/api/3/search";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token.getAccessToken());
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");

            String jql = "ORDER BY created DESC";
            String requestBody = "{\"jql\": \"" + jql + "\", \"maxResults\": 50}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            org.json.JSONObject jsonResponse = new org.json.JSONObject(response.getBody());
            org.json.JSONArray issues = jsonResponse.getJSONArray("issues");
            java.util.List<java.util.Map<String, Object>> issueList = new java.util.ArrayList<>();
            for (int i = 0; i < issues.length(); i++) {
                org.json.JSONObject issue = issues.getJSONObject(i);
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("id", issue.getString("id"));
                map.put("key", issue.getString("key"));
                map.put("summary", issue.getJSONObject("fields").getString("summary"));
                map.put("description", issue.getJSONObject("fields").optString("description"));
                issueList.add(map);
            }
            return java.util.Map.of("issues", issueList);
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public Map<String, Object> createIssue(String enterpriseId, String summary, String description, String issueType) {
        JiraOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "JIRA not connected"));

        try {
            String url = token.getSiteUrl() + "/rest/api/3/issue";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token.getAccessToken());
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");

            org.json.JSONObject fields = new org.json.JSONObject();
            fields.put("project", new org.json.JSONObject().put("key", "TEST"));
            fields.put("summary", summary);
            fields.put("description", description);
            fields.put("issuetype", new org.json.JSONObject().put("name", issueType));

            org.json.JSONObject requestBody = new org.json.JSONObject();
            requestBody.put("fields", fields);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            org.json.JSONObject createdIssue = new org.json.JSONObject(response.getBody());
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("id", createdIssue.getString("id"));
            result.put("key", createdIssue.getString("key"));
            result.put("self", createdIssue.getString("self"));
            return result;
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public JiraOAuthToken refreshToken(String enterpriseId) {
        JiraOAuthToken existing = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "JIRA not connected"));

        if (existing.getRefreshToken() == null || existing.getRefreshToken().isEmpty()) {
            throw new OAuthException("no_refresh_token", "Refresh token not available");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.util.LinkedMultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("client_id", oAuthConfig.getJiraClientId());
        params.add("client_secret", oAuthConfig.getJiraClientSecret());
        params.add("refresh_token", existing.getRefreshToken());
        params.add("grant_type", "refresh_token");

        HttpEntity<org.springframework.util.MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            java.util.Map<String, Object> response = restTemplate.postForObject(
                    oAuthConfig.getJiraTokenUrl(),
                    entity,
                    java.util.Map.class
            );

            if (response == null || !response.containsKey("access_token")) {
                throw new OAuthException("api_error", "No access token in response");
            }

            existing.setAccessToken((String) response.get("access_token"));
            if (response.containsKey("refresh_token")) {
                existing.setRefreshToken((String) response.get("refresh_token"));
            }
            if (response.containsKey("expires_in")) {
                existing.setExpiresIn((Integer) response.get("expires_in"));
            }

            tokenRepository.save(existing);
            return existing;
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }
}
