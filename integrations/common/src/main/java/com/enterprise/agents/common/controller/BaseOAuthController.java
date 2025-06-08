package com.enterprise.agents.common.controller;

import com.enterprise.agents.common.model.IntegrationType;
import com.enterprise.agents.common.service.IntegrationLoggingService;
import com.enterprise.agents.common.service.IntegrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/oauth")
public abstract class BaseOAuthController {
    protected final IntegrationService integrationService;
    protected final IntegrationLoggingService loggingService;

    protected BaseOAuthController(
            IntegrationService integrationService,
            IntegrationLoggingService loggingService) {
        this.integrationService = integrationService;
        this.loggingService = loggingService;
    }

    @GetMapping("/url")
    public ResponseEntity<Map<String, String>> getOAuthUrl(
            @RequestParam Long companyId,
            @RequestParam IntegrationType type,
            HttpSession session) {
        // Validate session
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        // Generate state for CSRF protection
        String state = UUID.randomUUID().toString();
        session.setAttribute("oauth_state", state);

        // Log OAuth initiation
        loggingService.logOAuthInitiation(companyId, type, state);

        // Get OAuth URL
        String url = integrationService.generateOAuthUrl(companyId, type, state);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, String>> handleCallback(
            @RequestParam Long companyId,
            @RequestParam IntegrationType type,
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session) {
        // Validate session
        if (session.getAttribute("user") == null) {
            loggingService.logOAuthFailure(companyId, type, state, "Not authenticated");
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        // Validate state
        String savedState = (String) session.getAttribute("oauth_state");
        if (savedState == null || !savedState.equals(state)) {
            loggingService.logOAuthFailure(companyId, type, state, "Invalid state");
            return ResponseEntity.status(400).body(Map.of("error", "Invalid state"));
        }

        try {
            integrationService.handleOAuthCallback(companyId, type, code, state);
            loggingService.logOAuthSuccess(companyId, type, state);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            loggingService.logOAuthFailure(companyId, type, state, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> checkStatus(
            @RequestParam Long companyId,
            @RequestParam IntegrationType type) {
        boolean isConnected = integrationService.isConnected(companyId, type);
        return ResponseEntity.ok(Map.of("connected", isConnected));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect(
            @RequestParam Long companyId,
            @RequestParam IntegrationType type) {
        try {
            integrationService.disconnect(companyId, type);
            loggingService.logDisconnection(companyId, type);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
} 