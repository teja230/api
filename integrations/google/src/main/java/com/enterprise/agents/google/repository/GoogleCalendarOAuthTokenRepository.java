package com.enterprise.agents.google.repository;

import com.enterprise.agents.google.model.GoogleCalendarOAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoogleCalendarOAuthTokenRepository extends JpaRepository<GoogleCalendarOAuthToken, Long> {
    Optional<GoogleCalendarOAuthToken> findByEnterpriseId(String enterpriseId);
} 