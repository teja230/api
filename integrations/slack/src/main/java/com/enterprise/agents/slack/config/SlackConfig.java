package com.enterprise.agents.slack.config;

import com.enterprise.agents.common.config.OAuthConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "slack")
public class SlackConfig extends OAuthConfig {
    private String botToken;
    private String signingSecret;
    private String verificationToken;
} 