package com.enterprise.agents.common.repository;

import com.enterprise.agents.common.model.IntegrationConfiguration;
import com.enterprise.agents.common.model.IntegrationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationConfigurationRepository extends JpaRepository<IntegrationConfiguration, Long> {
    Optional<IntegrationConfiguration> findByCompany_IdAndType(Long companyId, IntegrationType type);

    boolean existsByCompany_IdAndType(Long companyId, IntegrationType type);

    void deleteByCompany_IdAndType(Long companyId, IntegrationType type);
} 