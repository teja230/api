package com.enterprise.agents.common.service;

import com.enterprise.agents.common.model.IntegrationToken;
import com.enterprise.agents.common.model.IntegrationType;
import com.enterprise.agents.common.repository.IntegrationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenManagementService {
    private final IntegrationTokenRepository tokenRepository;
    private final TokenEncryptionService encryptionService;

    public TokenManagementService(
            IntegrationTokenRepository tokenRepository,
            TokenEncryptionService encryptionService) {
        this.tokenRepository = tokenRepository;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public IntegrationToken storeToken(Long companyId, IntegrationType type, String accessToken,
                                       String refreshToken, String tokenType, LocalDateTime expiresAt, String scopes) {
        try {
            // Encrypt tokens before storage
            String encryptedAccessToken = encryptionService.encrypt(accessToken);
            String encryptedRefreshToken = refreshToken != null ?
                    encryptionService.encrypt(refreshToken) : null;

            // Delete any existing tokens
            tokenRepository.deleteByCompany_IdAndType(companyId, type);

            // Create and save new token
            IntegrationToken token = new IntegrationToken();
            token.setCompanyId(companyId);
            token.setType(type);
            token.setAccessToken(encryptedAccessToken);
            token.setRefreshToken(encryptedRefreshToken);
            token.setTokenType(tokenType);
            token.setExpiresAt(expiresAt);
            token.setScopes(scopes);

            return tokenRepository.save(token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store token", e);
        }
    }

    public Optional<String> getValidAccessToken(Long companyId, IntegrationType type) {
        try {
            return tokenRepository.findValidToken(companyId, type)
                    .map(token -> {
                        try {
                            return encryptionService.decrypt(token.getAccessToken());
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to decrypt token", e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve token", e);
        }
    }

    public Optional<String> getRefreshToken(Long companyId, IntegrationType type) {
        try {
            return tokenRepository.findByCompany_IdAndType(companyId, type)
                    .map(token -> {
                        try {
                            return token.getRefreshToken() != null ?
                                    encryptionService.decrypt(token.getRefreshToken()) : null;
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to decrypt refresh token", e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve refresh token", e);
        }
    }

    public LocalDateTime getExpiresAt(Long companyId, IntegrationType type) {
        return tokenRepository.findByCompany_IdAndType(companyId, type)
                .map(IntegrationToken::getExpiresAt)
                .orElse(null);
    }

    @Transactional
    public void deleteToken(Long companyId, IntegrationType type) {
        tokenRepository.deleteByCompany_IdAndType(companyId, type);
    }

    public boolean hasValidToken(Long companyId, IntegrationType type) {
        return tokenRepository.findValidToken(companyId, type).isPresent();
    }
} 