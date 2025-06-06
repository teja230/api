package com.enterprise.agents.common.repository;

import com.enterprise.agents.common.model.CompanyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyConfigRepository extends JpaRepository<CompanyConfig, Long> {
    Optional<CompanyConfig> findByCompanyId(String companyId);

    boolean existsByCompanyId(String companyId);
} 