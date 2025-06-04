package com.enterprise.agents.slack;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    OAuthToken findByTeamId(String teamId);
}

