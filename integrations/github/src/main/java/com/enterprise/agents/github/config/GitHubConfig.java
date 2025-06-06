package com.enterprise.agents.github.config;

import com.enterprise.agents.common.config.OAuthConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "github")
public class GitHubConfig extends OAuthConfig {
    private String apiVersion;
    private String defaultBranch;
    private String defaultOrganization;
} 