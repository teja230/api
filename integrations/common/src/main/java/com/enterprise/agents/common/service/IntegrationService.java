package com.enterprise.agents.common.service;

import com.enterprise.agents.common.model.IntegrationConfiguration;
import com.enterprise.agents.common.model.IntegrationType;

public interface IntegrationService {
    /**
     * Generate the OAuth URL for the integration
     */
    String generateOAuthUrl(Long companyId, IntegrationType type, String state);

    /**
     * Handle the OAuth callback
     */
    void handleOAuthCallback(Long companyId, IntegrationType type, String code, String state);

    /**
     * Check if the integration is connected
     */
    boolean isConnected(Long companyId, IntegrationType type);

    /**
     * Disconnect the integration
     */
    void disconnect(Long companyId, IntegrationType type);

    /**
     * Create or update integration configuration
     */
    IntegrationConfiguration configure(Long companyId, IntegrationType type, String clientId, String clientSecret);
} 