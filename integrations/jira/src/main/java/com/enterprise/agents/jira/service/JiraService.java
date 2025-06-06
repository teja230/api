package com.enterprise.agents.jira.service;

import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.repository.JiraOAuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JiraService {
    private final JiraOAuthTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    public void saveToken(JiraOAuthToken token) {
        tokenRepository.save(token);
    }

    public boolean isConnected(String enterpriseId) {
        return tokenRepository.findByEnterpriseId(enterpriseId).isPresent();
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

    public JiraOAuthToken exchangeCodeForToken(String code, String enterpriseId) {
        // Implementation for OAuth token exchange
        return null; // TODO: Implement OAuth token exchange
    }

    public JiraOAuthToken refreshToken(String enterpriseId) {
        // Implementation for token refresh
        return null; // TODO: Implement token refresh
    }
}
