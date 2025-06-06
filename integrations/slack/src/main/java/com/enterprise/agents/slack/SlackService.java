package com.enterprise.agents.slack;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlackService {
    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    public void postMessage(String channel, String message) {
        try {
            Payload payload = Payload.builder().channel(channel).text(message).build();
            Slack.getInstance().send(slackWebhookUrl, payload);
        } catch (Exception e) {
            // Log error
        }
    }
}

