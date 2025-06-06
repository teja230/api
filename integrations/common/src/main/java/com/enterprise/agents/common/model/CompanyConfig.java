package com.enterprise.agents.common.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@Table(name = "company_configs")
public class CompanyConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String companyId;

    @Column(nullable = false)
    private String companyName;

    @Column
    private String logoUrl;

    @Column
    private String primaryColor;

    @Column
    private String secondaryColor;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "company_config_settings",
            joinColumns = @JoinColumn(name = "company_config_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value")
    private Map<String, String> settings = new HashMap<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Integration-specific settings
    @Column
    private String slackWorkspaceId;

    @Column
    private String jiraSiteUrl;

    @Column
    private String githubOrgId;

    @Column
    private String googleWorkspaceId;

    @Column
    private String onboardingTemplateId;

    // Integration enable flags
    @Column
    private boolean enableSlackNotifications;
    @Column
    private boolean enableJiraIntegration;
    @Column
    private boolean enableGitHubIntegration;
    @Column
    private boolean enableGoogleCalendarIntegration;

    @Column
    private String defaultTimezone;

    @Column
    private String defaultLanguage;

    public boolean getEnableSlackNotifications() {
        return enableSlackNotifications;
    }

    public void setEnableSlackNotifications(boolean enableSlackNotifications) {
        this.enableSlackNotifications = enableSlackNotifications;
    }

    public boolean getEnableJiraIntegration() {
        return enableJiraIntegration;
    }

    public void setEnableJiraIntegration(boolean enableJiraIntegration) {
        this.enableJiraIntegration = enableJiraIntegration;
    }

    public boolean getEnableGitHubIntegration() {
        return enableGitHubIntegration;
    }

    public void setEnableGitHubIntegration(boolean enableGitHubIntegration) {
        this.enableGitHubIntegration = enableGitHubIntegration;
    }

    public boolean getEnableGoogleCalendarIntegration() {
        return enableGoogleCalendarIntegration;
    }

    public void setEnableGoogleCalendarIntegration(boolean enableGoogleCalendarIntegration) {
        this.enableGoogleCalendarIntegration = enableGoogleCalendarIntegration;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getJiraSiteUrl() {
        return jiraSiteUrl;
    }

    public String getGithubOrgId() {
        return githubOrgId;
    }

    public String getGoogleWorkspaceId() {
        return googleWorkspaceId;
    }

    public String getOnboardingTemplateId() {
        return onboardingTemplateId;
    }

    public String getDefaultTimezone() {
        return defaultTimezone;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
}
