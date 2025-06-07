package com.enterprise.agents.jira.repository;

import com.enterprise.agents.jira.model.JiraOAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JiraOAuthTokenRepository extends JpaRepository<JiraOAuthToken, Long> {
    Optional<JiraOAuthToken> findByEnterpriseId(String enterpriseId);

    void deleteByEnterpriseId(String enterpriseId);
} 