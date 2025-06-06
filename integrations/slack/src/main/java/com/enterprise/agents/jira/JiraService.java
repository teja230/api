package com.enterprise.agents.jira;

import com.enterprise.agents.config.EnterpriseConfig;
import com.enterprise.agents.config.EnterpriseConfigRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class JiraService {
    private final OAuthTokenRepository tokenRepository;
    private final EnterpriseConfigRepository configRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public JiraService(OAuthTokenRepository tokenRepository, EnterpriseConfigRepository configRepository) {
        this.tokenRepository = tokenRepository;
        this.configRepository = configRepository;
        this.restTemplate = new RestTemplate();
    }

    public List<Map<String, Object>> getOnboardingIssues(String enterpriseId) {
        EnterpriseConfig config = configRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise configuration not found"));

        List<OAuthToken> tokens = tokenRepository.findByEnterpriseId(enterpriseId);
        if (tokens.isEmpty()) {
            throw new RuntimeException("No JIRA connection found for enterprise");
        }

        OAuthToken token = tokens.get(0);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        headers.set("Accept", "application/json");

        String jql = "project = " + token.getProjectKey() +
                " AND labels = onboarding" +
                " ORDER BY created DESC";
        String url = config.getJiraBaseUrl() + "/rest/api/3/search?jql=" +
                URLEncoder.encode(jql, StandardCharsets.UTF_8);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        JSONObject json = new JSONObject(response.getBody());
        JSONArray issues = json.getJSONArray("issues");
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.getJSONObject(i);
            JSONObject fields = issue.getJSONObject("fields");

            result.add(Map.of(
                    "id", issue.getString("id"),
                    "key", issue.getString("key"),
                    "summary", fields.getString("summary"),
                    "description", fields.optString("description", ""),
                    "status", fields.getJSONObject("status").getString("name"),
                    "assignee", fields.optJSONObject("assignee") != null ?
                            fields.getJSONObject("assignee").getString("displayName") : "Unassigned"
            ));
        }

        return result;
    }

    public void createOnboardingIssue(String enterpriseId, String summary, String description,
                                      String assignee, List<String> labels) {
        EnterpriseConfig config = configRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise configuration not found"));

        List<OAuthToken> tokens = tokenRepository.findByEnterpriseId(enterpriseId);
        if (tokens.isEmpty()) {
            throw new RuntimeException("No JIRA connection found for enterprise");
        }

        OAuthToken token = tokens.get(0);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        headers.set("Content-Type", "application/json");

        JSONObject issue = new JSONObject();
        JSONObject fields = new JSONObject();

        fields.put("project", new JSONObject().put("key", token.getProjectKey()));
        fields.put("summary", summary);
        fields.put("description", description);

        if (assignee != null) {
            fields.put("assignee", new JSONObject().put("id", assignee));
        }

        JSONArray labelsArray = new JSONArray();
        labelsArray.put("onboarding");
        for (String label : labels) {
            labelsArray.put(label);
        }
        fields.put("labels", labelsArray);

        issue.put("fields", fields);

        String url = config.getJiraBaseUrl() + "/rest/api/3/issue";
        restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(issue.toString(), headers),
                String.class
        );
    }

    public void updateIssueStatus(String enterpriseId, String issueId, String statusId) {
        EnterpriseConfig config = configRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise configuration not found"));

        List<OAuthToken> tokens = tokenRepository.findByEnterpriseId(enterpriseId);
        if (tokens.isEmpty()) {
            throw new RuntimeException("No JIRA connection found for enterprise");
        }

        OAuthToken token = tokens.get(0);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        headers.set("Content-Type", "application/json");

        JSONObject transition = new JSONObject();
        transition.put("transition", new JSONObject().put("id", statusId));

        String url = config.getJiraBaseUrl() + "/rest/api/3/issue/" + issueId + "/transitions";
        restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(transition.toString(), headers),
                String.class
        );
    }
} 