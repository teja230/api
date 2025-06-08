package com.enterprise.agents.common.repository;

import com.enterprise.agents.common.model.IntegrationToken;
import com.enterprise.agents.common.model.IntegrationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IntegrationTokenRepository extends JpaRepository<IntegrationToken, Long> {
    Optional<IntegrationToken> findByCompany_IdAndType(Long companyId, IntegrationType type);

    @Query("SELECT t FROM IntegrationToken t WHERE t.company.id = ?1 AND t.type = ?2 AND (t.expiresAt IS NULL OR t.expiresAt > CURRENT_TIMESTAMP)")
    Optional<IntegrationToken> findValidToken(Long companyId, IntegrationType type);

    void deleteByCompany_IdAndType(Long companyId, IntegrationType type);
} 