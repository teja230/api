package com.enterprise.agents.github.repository;

import com.enterprise.agents.common.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByDomain(String domain);
} 