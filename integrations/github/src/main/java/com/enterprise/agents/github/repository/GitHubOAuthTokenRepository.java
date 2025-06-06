package com.enterprise.agents.github.repository;

import com.enterprise.agents.github.model.GitHubOAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GitHubOAuthTokenRepository extends JpaRepository<GitHubOAuthToken, Long> {
    Optional<GitHubOAuthToken> findByEnterpriseId(String enterpriseId);

    Optional<GitHubOAuthToken> findById(Long id);
} 