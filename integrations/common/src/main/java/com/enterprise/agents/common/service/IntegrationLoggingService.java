package com.enterprise.agents.common.service;

import com.enterprise.agents.common.model.IntegrationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IntegrationLoggingService {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationLoggingService.class);
    private final Map<String, IntegrationMetrics> metricsMap = new ConcurrentHashMap<>();

    public void logOAuthInitiation(Long companyId, IntegrationType type, String state) {
        String key = getMetricsKey(companyId, type);
        metricsMap.computeIfAbsent(key, k -> new IntegrationMetrics());
        metricsMap.get(key).incrementOAuthAttempts();

        logger.info("OAuth flow initiated - Company: {}, Integration: {}, State: {}",
                companyId, type, state);
    }

    public void logOAuthSuccess(Long companyId, IntegrationType type, String state) {
        String key = getMetricsKey(companyId, type);
        metricsMap.computeIfAbsent(key, k -> new IntegrationMetrics());
        metricsMap.get(key).incrementSuccessfulConnections();
        metricsMap.get(key).setLastSuccessfulConnection(LocalDateTime.now());

        logger.info("OAuth flow completed successfully - Company: {}, Integration: {}, State: {}",
                companyId, type, state);
    }

    public void logOAuthFailure(Long companyId, IntegrationType type, String state, String error) {
        String key = getMetricsKey(companyId, type);
        metricsMap.computeIfAbsent(key, k -> new IntegrationMetrics());
        metricsMap.get(key).incrementFailedConnections();

        logger.error("OAuth flow failed - Company: {}, Integration: {}, State: {}, Error: {}",
                companyId, type, state, error);
    }

    public void logTokenRefresh(Long companyId, IntegrationType type, boolean success) {
        String key = getMetricsKey(companyId, type);
        metricsMap.computeIfAbsent(key, k -> new IntegrationMetrics());

        if (success) {
            metricsMap.get(key).incrementSuccessfulRefreshes();
            metricsMap.get(key).setLastSuccessfulRefresh(LocalDateTime.now());
            logger.info("Token refresh successful - Company: {}, Integration: {}",
                    companyId, type);
        } else {
            metricsMap.get(key).incrementFailedRefreshes();
            logger.error("Token refresh failed - Company: {}, Integration: {}",
                    companyId, type);
        }
    }

    public void logDisconnection(Long companyId, IntegrationType type) {
        String key = getMetricsKey(companyId, type);
        metricsMap.computeIfAbsent(key, k -> new IntegrationMetrics());
        metricsMap.get(key).incrementDisconnections();

        logger.info("Integration disconnected - Company: {}, Integration: {}",
                companyId, type);
    }

    public IntegrationMetrics getMetrics(Long companyId, IntegrationType type) {
        return metricsMap.getOrDefault(getMetricsKey(companyId, type), new IntegrationMetrics());
    }

    private String getMetricsKey(Long companyId, IntegrationType type) {
        return companyId + ":" + type.name();
    }

    public static class IntegrationMetrics {
        private int oAuthAttempts;
        private int successfulConnections;
        private int failedConnections;
        private int successfulRefreshes;
        private int failedRefreshes;
        private int disconnections;
        private LocalDateTime lastSuccessfulConnection;
        private LocalDateTime lastSuccessfulRefresh;

        public void incrementOAuthAttempts() {
            oAuthAttempts++;
        }

        public void incrementSuccessfulConnections() {
            successfulConnections++;
        }

        public void incrementFailedConnections() {
            failedConnections++;
        }

        public void incrementSuccessfulRefreshes() {
            successfulRefreshes++;
        }

        public void incrementFailedRefreshes() {
            failedRefreshes++;
        }

        public void incrementDisconnections() {
            disconnections++;
        }

        // Getters
        public int getOAuthAttempts() {
            return oAuthAttempts;
        }

        public int getSuccessfulConnections() {
            return successfulConnections;
        }

        public int getFailedConnections() {
            return failedConnections;
        }

        public int getSuccessfulRefreshes() {
            return successfulRefreshes;
        }

        public int getFailedRefreshes() {
            return failedRefreshes;
        }

        public int getDisconnections() {
            return disconnections;
        }

        public LocalDateTime getLastSuccessfulConnection() {
            return lastSuccessfulConnection;
        }

        public void setLastSuccessfulConnection(LocalDateTime time) {
            this.lastSuccessfulConnection = time;
        }

        public LocalDateTime getLastSuccessfulRefresh() {
            return lastSuccessfulRefresh;
        }

        public void setLastSuccessfulRefresh(LocalDateTime time) {
            this.lastSuccessfulRefresh = time;
        }
    }
} 