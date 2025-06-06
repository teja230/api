package com.enterprise.agents.jira.config;

import com.enterprise.agents.common.config.OAuthConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jira")
public class JiraConfig extends OAuthConfig {
    private String apiVersion;
    private String defaultProjectKey;
    private String defaultIssueType;
} 