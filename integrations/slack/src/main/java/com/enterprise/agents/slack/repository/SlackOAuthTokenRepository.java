package com.enterprise.agents.slack.repository;

import com.enterprise.agents.slack.model.SlackOAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlackOAuthTokenRepository extends JpaRepository<SlackOAuthToken, Long> {
    Optional<SlackOAuthToken> findByEnterpriseId(String enterpriseId);

    Optional<SlackOAuthToken> findByTeamId(String teamId);
} 