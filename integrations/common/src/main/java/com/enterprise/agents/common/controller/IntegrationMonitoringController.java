package com.enterprise.agents.common.controller;

import com.enterprise.agents.common.model.IntegrationType;
import com.enterprise.agents.common.service.IntegrationLoggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/monitoring/integrations")
public class IntegrationMonitoringController {
    private final IntegrationLoggingService loggingService;

    public IntegrationMonitoringController(IntegrationLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @GetMapping("/metrics/{companyId}/{type}")
    public ResponseEntity<Map<String, Object>> getMetrics(
            @PathVariable Long companyId,
            @PathVariable IntegrationType type) {
        var metrics = loggingService.getMetrics(companyId, type);
        return ResponseEntity.ok(Map.of(
                "oAuthAttempts", metrics.getOAuthAttempts(),
                "successfulConnections", metrics.getSuccessfulConnections(),
                "failedConnections", metrics.getFailedConnections(),
                "successfulRefreshes", metrics.getSuccessfulRefreshes(),
                "failedRefreshes", metrics.getFailedRefreshes(),
                "disconnections", metrics.getDisconnections(),
                "lastSuccessfulConnection", metrics.getLastSuccessfulConnection(),
                "lastSuccessfulRefresh", metrics.getLastSuccessfulRefresh()
        ));
    }

    @GetMapping("/health/{companyId}/{type}")
    public ResponseEntity<Map<String, Object>> getHealth(
            @PathVariable Long companyId,
            @PathVariable IntegrationType type) {
        var metrics = loggingService.getMetrics(companyId, type);
        boolean isHealthy = metrics.getSuccessfulConnections() > 0 &&
                metrics.getFailedConnections() < metrics.getSuccessfulConnections();

        return ResponseEntity.ok(Map.of(
                "status", isHealthy ? "HEALTHY" : "UNHEALTHY",
                "lastSuccessfulConnection", metrics.getLastSuccessfulConnection(),
                "lastSuccessfulRefresh", metrics.getLastSuccessfulRefresh(),
                "successRate", calculateSuccessRate(metrics)
        ));
    }

    private double calculateSuccessRate(IntegrationLoggingService.IntegrationMetrics metrics) {
        int total = metrics.getSuccessfulConnections() + metrics.getFailedConnections();
        return total > 0 ? (double) metrics.getSuccessfulConnections() / total : 0.0;
    }
} 