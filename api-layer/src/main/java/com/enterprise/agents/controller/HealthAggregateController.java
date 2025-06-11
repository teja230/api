package com.enterprise.agents.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthAggregateController {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${service.github.health-url:http://localhost:8081/api/github/actuator/health}")
    private String githubHealthUrl;
    @Value("${service.google.health-url:http://localhost:8082/actuator/health}")
    private String googleHealthUrl;
    @Value("${service.slack.health-url:http://localhost:8083/actuator/health}")
    private String slackHealthUrl;
    @Value("${service.jira.health-url:http://localhost:8084/api/jira/actuator/health}")
    private String jiraHealthUrl;

    @GetMapping("/api/health/aggregate")
    public ResponseEntity<Map<String, Object>> aggregateHealth() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        boolean allUp = true;

        // Check API layer health
        Map<String, Object> apiStatus = new HashMap<>();
        apiStatus.put("status", "UP");
        apiStatus.put("lastChecked", Instant.now().toString());
        details.put("api", apiStatus);

        // Check GitHub health
        Map<String, Object> githubStatus = checkHealthWithDetails(githubHealthUrl);
        details.put("github", githubStatus);
        allUp = allUp && "UP".equals(githubStatus.get("status"));

        // Check Google health
        Map<String, Object> googleStatus = checkHealthWithDetails(googleHealthUrl);
        details.put("google", googleStatus);
        allUp = allUp && "UP".equals(googleStatus.get("status"));

        // Check Slack health
        Map<String, Object> slackStatus = checkHealthWithDetails(slackHealthUrl);
        details.put("slack", slackStatus);
        allUp = allUp && "UP".equals(slackStatus.get("status"));

        // Check Jira health
        Map<String, Object> jiraStatus = checkHealthWithDetails(jiraHealthUrl);
        details.put("jira", jiraStatus);
        allUp = allUp && "UP".equals(jiraStatus.get("status"));

        result.put("status", allUp ? "UP" : "DOWN");
        result.put("details", details);
        result.put("timestamp", Instant.now().toString());

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> checkHealthWithDetails(String url) {
        Map<String, Object> status = new HashMap<>();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> healthData = response.getBody();
                String healthStatus = (String) healthData.get("status");
                status.put("status", healthStatus);
                status.put("lastChecked", Instant.now().toString());
                status.put("details", healthData);
            } else {
                status.put("status", "DOWN");
                status.put("error", "Service returned non-200 status: " + response.getStatusCode());
                status.put("lastChecked", Instant.now().toString());
            }
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            status.put("lastChecked", Instant.now().toString());
        }
        return status;
    }
}
