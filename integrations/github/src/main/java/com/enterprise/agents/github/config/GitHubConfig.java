package com.enterprise.agents.github.config;

import com.enterprise.agents.common.config.OAuthConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Data
@Configuration
@ConfigurationProperties(prefix = "github")
@Primary
public class GitHubConfig extends OAuthConfig {
    private String apiVersion;
    private String defaultBranch;
    private String defaultOrganization;
    private String clientId;
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
} 