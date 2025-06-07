package com.enterprise.agents.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SsoMockController {
    @GetMapping("/api/sso/login")
    public ResponseEntity<?> mockSsoLogin(HttpServletRequest request, HttpSession session) {
        String remoteAddr = request.getRemoteAddr();
        // Allow only localhost for mock SSO
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(request.getServerName())) {
            // Simulate authentication by setting a session attribute
            session.setAttribute("user", "user@company.com");
            // Optionally, you can redirect to the UI root or return a JSON response
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "user", "user@company.com"
            ));
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Mock SSO only allowed from localhost"));
        }
    }

    @GetMapping("/api/sso/user")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user != null) {
            return ResponseEntity.ok(Map.of("user", user));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
    }
}
