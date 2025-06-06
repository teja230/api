package com.enterprise.agents.jira;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
public class JiraOAuthController {
    private static final Logger logger = LoggerFactory.getLogger(JiraOAuthController.class);
    private final OAuthTokenRepository tokenRepository;
    private final EnterpriseConfigRepository configRepository;
    @Value("${jira.oauth.client-id}")
    private String clientId;
    @Value("${jira.oauth.client-secret}")
    private String clientSecret;
    @Value("${jira.oauth.redirect-uri}")
    private String redirectUri;
    @Value("${jira.oauth.scopes:read:jira-work,write:jira-work,manage:jira-configuration}")
    private String scopes;

    @Autowired
    public JiraOAuthController(OAuthTokenRepository tokenRepository, EnterpriseConfigRepository configRepository) {
        this.tokenRepository = tokenRepository;
        this.configRepository = configRepository;
    }

    @GetMapping("/api/jira/oauth/url")
    public Map<String, String> getJiraOAuthUrl(@RequestParam String enterpriseId) {
        EnterpriseConfig config = configRepository.findById(enterpriseId)
                .orElseThrow(() -> new RuntimeException("Enterprise configuration not found"));

        String url = config.getJiraBaseUrl() + "/oauth/authorize?" +
                "client_id=" + clientId +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&state=" + URLEncoder.encode(enterpriseId, StandardCharsets.UTF_8);
        return Map.of("url", url);
    }

    @GetMapping("/api/jira/status")
    public Map<String, Object> getJiraStatus(@RequestParam String enterpriseId) {
        boolean connected = tokenRepository.findByEnterpriseId(enterpriseId).isPresent();
        return Map.of("connected", connected);
    }

    @GetMapping("/api/jira/projects")
    public List<Map<String, String>> getConnectedProjects(@RequestParam String enterpriseId) {
        List<OAuthToken> tokens = tokenRepository.findByEnterpriseId(enterpriseId);
        return tokens.stream()
                .map(t -> Map.of(
                        "projectId", t.getProjectId(),
                        "projectName", t.getProjectName(),
                        "projectKey", t.getProjectKey()
                ))
                .toList();
    }

    @GetMapping("/api/jira/oauth/callback")
    public ResponseEntity<String> handleJiraCallback(
            @RequestParam String code,
            @RequestParam String state) {
        try {
            EnterpriseConfig config = configRepository.findById(state)
                    .orElseThrow(() -> new RuntimeException("Enterprise configuration not found"));

            // Exchange code for access token
            String tokenUrl = config.getJiraBaseUrl() + "/oauth/token";
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(
                    tokenUrl,
                    Map.of(
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "code", code,
                            "redirect_uri", redirectUri,
                            "grant_type", "authorization_code"
                    ),
                    String.class
            );

            JSONObject tokenJson = new JSONObject(response);
            String accessToken = tokenJson.getString("access_token");
            String refreshToken = tokenJson.getString("refresh_token");

            // Get project info
            String projectUrl = config.getJiraBaseUrl() + "/rest/api/3/project";
            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("Authorization", "Bearer " + accessToken);
                return execution.execute(request, body);
            });
            String projectResponse = restTemplate.getForObject(projectUrl, String.class);
            JSONArray projects = new JSONArray(projectResponse);

            // Save tokens for each project
            for (int i = 0; i < projects.length(); i++) {
                JSONObject project = projects.getJSONObject(i);
                OAuthToken token = new OAuthToken();
                token.setEnterpriseId(state);
                token.setAccessToken(accessToken);
                token.setRefreshToken(refreshToken);
                token.setProjectId(project.getString("id"));
                token.setProjectKey(project.getString("key"));
                token.setProjectName(project.getString("name"));
                tokenRepository.save(token);
            }

            logger.info("JIRA access tokens saved for enterprise {} with {} projects.",
                    state, projects.length());

            // Redirect to frontend with success
            String redirectUrl = "/success?status=ok&enterprise=" +
                    URLEncoder.encode(state, StandardCharsets.UTF_8);
            return ResponseEntity.status(302).header("Location", redirectUrl).build();

        } catch (Exception e) {
            logger.error("Exception during JIRA OAuth callback", e);
            String redirectUrl = "/success?status=error&msg=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(302).header("Location", redirectUrl).build();
        }
    }
} 