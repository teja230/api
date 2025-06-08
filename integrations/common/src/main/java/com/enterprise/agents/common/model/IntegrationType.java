package com.enterprise.agents.common.model;

public enum IntegrationType {
    GITHUB("GitHub", "github.com", "repo,user,read:org"),
    SLACK("Slack", "slack.com", "channels:read,chat:write,users:read,team:read"),
    GOOGLE("Google", "google.com", "https://www.googleapis.com/auth/drive.file,https://www.googleapis.com/auth/calendar.events"),
    JIRA("JIRA", "atlassian.net", "read:jira-work,write:jira-work,read:jira-user");

    private final String displayName;
    private final String domain;
    private final String defaultScopes;

    IntegrationType(String displayName, String domain, String defaultScopes) {
        this.displayName = displayName;
        this.domain = domain;
        this.defaultScopes = defaultScopes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDomain() {
        return domain;
    }

    public String getDefaultScopes() {
        return defaultScopes;
    }

    public String getOAuthUrl() {
        return switch (this) {
            case GITHUB -> "https://github.com/login/oauth/authorize";
            case SLACK -> "https://slack.com/oauth/v2/authorize";
            case GOOGLE -> "https://accounts.google.com/o/oauth2/v2/auth";
            case JIRA -> "https://auth.atlassian.com/authorize";
        };
    }

    public String getTokenUrl() {
        return switch (this) {
            case GITHUB -> "https://github.com/login/oauth/access_token";
            case SLACK -> "https://slack.com/api/oauth.v2.access";
            case GOOGLE -> "https://oauth2.googleapis.com/token";
            case JIRA -> "https://auth.atlassian.com/oauth/token";
        };
    }
} 