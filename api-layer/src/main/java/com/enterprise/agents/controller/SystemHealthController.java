package com.enterprise.agents.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    private static final Map<String, String> SERVICE_URLS = Map.of(
            "github", "http://localhost:8080/api/github/actuator/health",
            "google-calendar", "http://localhost:8080/api/google/actuator/health",
            "slack", "http://localhost:8080/api/slack/actuator/health",
            "jira", "http://localhost:8080/api/jira/actuator/health"
    );
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> services = new HashMap<>();
        Instant now = Instant.now();

        SERVICE_URLS.forEach((serviceId, url) -> {
            Map<String, Object> serviceStatus = checkServiceHealth(url);
            serviceStatus.put("lastChecked", now.toString());
            services.put(serviceId, serviceStatus);
        });

        response.put("services", services);
        response.put("timestamp", now.toString());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> checkServiceHealth(String url) {
        Map<String, Object> status = new HashMap<>();
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> healthData = response.getBody();
                String healthStatus = (String) healthData.get("status");
                status.put("status", healthStatus);
                status.put("uptime", healthData.get("uptime"));
                status.put("details", healthData);
            } else {
                status.put("status", "DOWN");
                status.put("error", "Service returned non-200 status");
            }
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
        }
        return status;
    }
} 