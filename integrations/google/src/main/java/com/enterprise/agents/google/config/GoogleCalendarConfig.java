package com.enterprise.agents.google.config;

import com.enterprise.agents.common.config.OAuthConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google.calendar")
public class GoogleCalendarConfig extends OAuthConfig {
    private String applicationName;
    private String defaultTimezone;
    private String defaultCalendarId;
} 