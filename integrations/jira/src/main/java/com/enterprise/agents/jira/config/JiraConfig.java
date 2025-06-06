package com.enterprise.agents.jira.config;

import com.enterprise.agents.common.config.OAuthConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "jira")
@Primary
public class JiraConfig extends OAuthConfig {
    private String apiVersion;
    private String defaultProjectKey;
    private String defaultIssueType;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 