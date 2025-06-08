package com.enterprise.agents.common.service;

import com.enterprise.agents.common.model.IntegrationConfiguration;
import com.enterprise.agents.common.model.IntegrationType;
import com.enterprise.agents.common.repository.IntegrationConfigurationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TokenRefreshService {
    private final TokenManagementService tokenManagementService;
    private final IntegrationConfigurationRepository configRepository;
    private final IntegrationLoggingService loggingService;

    public TokenRefreshService(
            TokenManagementService tokenManagementService,
            IntegrationConfigurationRepository configRepository,
            IntegrationLoggingService loggingService) {
        this.tokenManagementService = tokenManagementService;
        this.configRepository = configRepository;
        this.loggingService = loggingService;
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void refreshExpiringTokens() {
        // Get all configurations
        List<IntegrationConfiguration> configs = configRepository.findAll();

        for (IntegrationConfiguration config : configs) {
            try {
                // Check if token needs refresh (expires in next 10 minutes)
                tokenManagementService.getValidAccessToken(config.getCompany().getId(), config.getType())
                        .ifPresent(token -> {
                            // If token exists but expires soon, refresh it
                            if (isExpiringSoon(config.getCompany().getId(), config.getType())) {
                                refreshToken(config);
                            }
                        });
            } catch (Exception e) {
                // Log error but continue with other tokens
                loggingService.logTokenRefresh(
                        config.getCompany().getId(),
                        config.getType(),
                        false
                );
            }
        }
    }

    private boolean isExpiringSoon(Long companyId, IntegrationType type) {
        return tokenManagementService.getValidAccessToken(companyId, type)
                .map(token -> {
                    // Check if token expires in next 10 minutes
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime expiresAt = tokenManagementService.getExpiresAt(companyId, type);
                    return expiresAt != null && expiresAt.isBefore(now.plusMinutes(10));
                })
                .orElse(false);
    }

    private void refreshToken(IntegrationConfiguration config) {
        // Get refresh token
        tokenManagementService.getRefreshToken(config.getCompany().getId(), config.getType())
                .ifPresent(refreshToken -> {
                    try {
                        // Call the appropriate refresh endpoint based on integration type
                        String newToken = refreshTokenForType(config, refreshToken);

                        // Store the new token
                        tokenManagementService.storeToken(
                                config.getCompany().getId(),
                                config.getType(),
                                newToken,
                                refreshToken, // Keep the same refresh token
                                "Bearer",
                                LocalDateTime.now().plusHours(1), // Example expiry
                                config.getScopes()
                        );

                        // Log successful refresh
                        loggingService.logTokenRefresh(
                                config.getCompany().getId(),
                                config.getType(),
                                true
                        );
                    } catch (Exception e) {
                        // Log failed refresh
                        loggingService.logTokenRefresh(
                                config.getCompany().getId(),
                                config.getType(),
                                false
                        );
                    }
                });
    }

    protected String refreshTokenForType(IntegrationConfiguration config, String refreshToken) {
        // This would be implemented differently for each integration type
        // For now, we'll throw an exception
        throw new UnsupportedOperationException(
                "Token refresh not implemented for " + config.getType());
    }
} 