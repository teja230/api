package com.enterprise.agents.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "oauth")
public class OAuthConfig {
    private String jiraAuthUrl;
    private String jiraClientId;
    private String jiraClientSecret;
    private String jiraRedirectUri;
    private String jiraScope;
    private String jiraTokenUrl;
    private String jiraApiUrl;

    private String githubAuthUrl;
    private String githubClientId;
    private String githubRedirectUri;
    private String githubScope;
    private String githubTokenUrl;
    private String githubApiUrl;

    private String googleAuthUrl;
    private String googleClientId;
    private String googleRedirectUri;
    private String googleScope;
    private String googleTokenUrl;
    private String googleApiUrl;

    public String getClientId() {
        return jiraClientId;
    }

    public String getClientSecret() {
        return jiraClientSecret;
    }

    public String getRedirectUri() {
        return jiraRedirectUri;
    }

    public String[] getScopes() {
        return new String[]{jiraScope};
    }

    public String getAuthorizationUrl() {
        return jiraAuthUrl;
    }

    public String getTokenUrl() {
        return jiraTokenUrl;
    }

    public String getUserInfoUrl() {
        return jiraApiUrl;
    }

    // Jira getters
    public String getJiraAuthUrl() {
        return jiraAuthUrl;
    }

    public String getJiraClientId() {
        return jiraClientId;
    }

    public String getJiraClientSecret() {
        return jiraClientSecret;
    }

    public String getJiraRedirectUri() {
        return jiraRedirectUri;
    }

    public String getJiraScope() {
        return jiraScope;
    }

    public String getJiraTokenUrl() {
        return jiraTokenUrl;
    }

    public String getJiraApiUrl() {
        return jiraApiUrl;
    }

    // GitHub getters
    public String getGithubAuthUrl() {
        return githubAuthUrl;
    }

    public String getGithubClientId() {
        return githubClientId;
    }

    public String getGithubRedirectUri() {
        return githubRedirectUri;
    }

    public String getGithubScope() {
        return githubScope;
    }

    public String getGithubTokenUrl() {
        return githubTokenUrl;
    }

    public String getGithubApiUrl() {
        return githubApiUrl;
    }

    // Google getters
    public String getGoogleAuthUrl() {
        return googleAuthUrl;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public String getGoogleRedirectUri() {
        return googleRedirectUri;
    }

    public String getGoogleScope() {
        return googleScope;
    }

    public String getGoogleTokenUrl() {
        return googleTokenUrl;
    }

    public String getGoogleApiUrl() {
        return googleApiUrl;
    }
}
