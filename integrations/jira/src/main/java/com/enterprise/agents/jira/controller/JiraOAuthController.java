package com.enterprise.agents.jira.controller;

import com.enterprise.agents.common.controller.BaseOAuthController;
import com.enterprise.agents.common.service.IntegrationLoggingService;
import com.enterprise.agents.common.service.IntegrationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JiraOAuthController extends BaseOAuthController {
    public JiraOAuthController(@Qualifier("jiraIntegrationService") IntegrationService integrationService, IntegrationLoggingService loggingService) {
        super(integrationService, loggingService);
    }
} 