package com.enterprise.agents.github.repository;

import com.enterprise.agents.github.model.OAuthConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthConfigurationRepository extends JpaRepository<OAuthConfiguration, Long> {
    Optional<OAuthConfiguration> findByCompanyIdAndProvider(Long companyId, String provider);

    boolean existsByCompanyIdAndProvider(Long companyId, String provider);
} 