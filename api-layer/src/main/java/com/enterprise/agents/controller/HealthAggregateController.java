package com.enterprise.agents.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthAggregateController {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${service.github.health-url:http://localhost:8082/api/github/actuator/health}")
    private String githubHealthUrl;
    @Value("${service.google.health-url:http://localhost:8083/api/google/actuator/health}")
    private String googleHealthUrl;
    @Value("${service.slack.health-url:http://localhost:8084/api/slack/actuator/health}")
    private String slackHealthUrl;
    @Value("${service.jira.health-url:http://localhost:8085/api/jira/actuator/health}")
    private String jiraHealthUrl;

    @GetMapping("/api/health/aggregate")
    public ResponseEntity<Map<String, Object>> aggregateHealth() {
        Map<String, Object> result = new HashMap<>();
        result.put("api", Map.of("status", "UP"));
        result.put("github", checkHealth(githubHealthUrl));
        result.put("google", checkHealth(googleHealthUrl));
        result.put("slack", checkHealth(slackHealthUrl));
        result.put("jira", checkHealth(jiraHealthUrl));
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> checkHealth(String url) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Map.of("status", response.getBody().getOrDefault("status", "UNKNOWN"));
            }
        } catch (Exception e) {
            // ignore
        }
        return Map.of("status", "DOWN");
    }
}

