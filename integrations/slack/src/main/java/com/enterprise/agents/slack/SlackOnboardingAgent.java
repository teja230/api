package com.enterprise.agents.slack;

import com.enterprise.agents.service.AuditService;
import com.enterprise.agents.service.OpenAIService;
import com.enterprise.agents.slack.service.SlackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlackOnboardingAgent {
    private final OpenAIService openAIService;
    private final SlackService slackService;
    private final AuditService auditService;

    @Value("${agents.onboarding.channel}")
    private String onboardingChannel;

    public SlackOnboardingAgent(OpenAIService openAIService, SlackService slackService, AuditService auditService) {
        this.openAIService = openAIService;
        this.slackService = slackService;
        this.auditService = auditService;
    }

    public void handleUserJoin(String userId, String userName) {
        String prompt = String.format("Generate a welcome message and onboarding task list for new employee %s.", userName);
        String onboardingMessage = openAIService.generateOnboardingMessage(prompt);
        slackService.sendMessage("enterpriseId", onboardingChannel, onboardingMessage);
        auditService.logEvent("onboarding", userId, onboardingMessage);
    }
}
