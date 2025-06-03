package com.enterprise.agents.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {
    @PostMapping("/trigger")
    public String triggerOnboarding(@RequestParam String userId, @RequestParam String userName) {
        return "Onboarding endpoint is currently unavailable.";
    }
}

