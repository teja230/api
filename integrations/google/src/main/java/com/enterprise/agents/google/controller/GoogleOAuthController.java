package com.enterprise.agents.google.controller;

import com.enterprise.agents.common.controller.BaseOAuthController;
import com.enterprise.agents.common.service.IntegrationLoggingService;
import com.enterprise.agents.common.service.IntegrationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/google")
public class GoogleOAuthController extends BaseOAuthController {
    public GoogleOAuthController(IntegrationService integrationService, IntegrationLoggingService loggingService) {
        super(integrationService, loggingService);
    }
} 