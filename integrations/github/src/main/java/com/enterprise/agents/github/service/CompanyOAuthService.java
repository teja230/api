package com.enterprise.agents.github.service;

import com.enterprise.agents.common.model.Company;
import com.enterprise.agents.github.model.OAuthConfiguration;
import com.enterprise.agents.github.repository.CompanyRepository;
import com.enterprise.agents.github.repository.OAuthConfigurationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CompanyOAuthService {
    private final CompanyRepository companyRepository;
    private final OAuthConfigurationRepository oauthConfigRepository;
    private final String defaultRedirectUri;

    public CompanyOAuthService(
            CompanyRepository companyRepository,
            OAuthConfigurationRepository oauthConfigRepository,
            @Value("${github.oauth.redirect-uri}") String defaultRedirectUri) {
        this.companyRepository = companyRepository;
        this.oauthConfigRepository = oauthConfigRepository;
        this.defaultRedirectUri = defaultRedirectUri;
    }

    @Transactional
    public OAuthConfiguration createOAuthConfig(Long companyId, String provider,
                                                String clientId, String clientSecret) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (oauthConfigRepository.existsByCompanyIdAndProvider(companyId, provider)) {
            throw new RuntimeException("OAuth configuration already exists for this company and provider");
        }

        OAuthConfiguration config = new OAuthConfiguration();
        config.setCompany(company);
        config.setProvider(provider);
        config.setClientId(clientId);
        config.setClientSecret(clientSecret);
        config.setRedirectUri(defaultRedirectUri);
        config.setScopes("repo,user"); // Default GitHub scopes

        return oauthConfigRepository.save(config);
    }

    public String generateOAuthUrl(Long companyId, String provider) {
        OAuthConfiguration config = oauthConfigRepository
                .findByCompanyIdAndProvider(companyId, provider)
                .orElseThrow(() -> new RuntimeException("OAuth configuration not found"));

        String state = UUID.randomUUID().toString();
        // Store state in Redis or DB for validation

        return String.format(
                "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=%s&state=%s",
                config.getClientId(),
                config.getRedirectUri(),
                config.getScopes(),
                state
        );
    }

    @Transactional
    public void handleOAuthCallback(Long companyId, String provider, String code, String state) {
        // Validate state
        // Exchange code for token
        // Store token
    }
} 