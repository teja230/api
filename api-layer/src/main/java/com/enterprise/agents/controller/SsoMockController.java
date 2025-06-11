package com.enterprise.agents.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sso")
public class SsoMockController {
    private static final Map<String, String> STATE_STORE = new HashMap<>();

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateSSO(
            HttpServletRequest request,
            HttpSession session) {
        // Generate a state parameter for security
        String state = UUID.randomUUID().toString();
        STATE_STORE.put(state, "company");

        // For mock purposes, we'll use a mock SSO URL
        String authUrl = "http://localhost:8080/api/sso/callback?code=mock-company-code&state=" + state;

        return ResponseEntity.ok(Map.of(
                "url", authUrl,
                "state", state
        ));
    }

    @GetMapping("/callback")
    public RedirectView handleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session) {

        if (error != null) {
            return new RedirectView("http://localhost:3000/login?error=" + error);
        }

        if (code == null || state == null) {
            return new RedirectView("http://localhost:3000/login?error=missing_parameters");
        }

        // Verify state
        String provider = STATE_STORE.remove(state);
        if (provider == null) {
            return new RedirectView("http://localhost:3000/login?error=invalid_state");
        }

        // For mock purposes, we'll simulate a successful authentication
        String email = "user@company.com";
        session.setAttribute("user", email);
        session.setAttribute("provider", "company");

        return new RedirectView("http://localhost:3000/dashboard");
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Object user = session.getAttribute("user");
        Object provider = session.getAttribute("provider");
        
        if (user != null) {
            return ResponseEntity.ok(Map.of(
                    "user", user,
                    "provider", provider
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpSession session) {
        return initiateSSO(request, session);
    }
}
