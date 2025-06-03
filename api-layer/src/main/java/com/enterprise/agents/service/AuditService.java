package com.enterprise.agents.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    public void logEvent(String eventType, String userId, String details) {
        // In production, persist to DB or audit log
        logger.info("AUDIT | type={} | user={} | details={}", eventType, userId, details);
    }
}

