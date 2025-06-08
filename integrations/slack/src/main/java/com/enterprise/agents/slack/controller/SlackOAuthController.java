package com.enterprise.agents.slack.controller;

import com.enterprise.agents.common.controller.BaseOAuthController;
import com.enterprise.agents.common.service.IntegrationLoggingService;
import com.enterprise.agents.common.service.IntegrationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/slack")
public class SlackOAuthController extends BaseOAuthController {
    public SlackOAuthController(@Qualifier("slackIntegrationService") IntegrationService integrationService, IntegrationLoggingService loggingService) {
        super(integrationService, loggingService);
    }
} 