package com.enterprise.agents.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "enterprise_configs")
public class EnterpriseConfig {
    @Id
    private String enterpriseId;
    private String enterpriseName;
    private String jiraBaseUrl;
    private String slackWorkspaceId;
    private String githubOrgId;
    private String googleWorkspaceId;
    private String onboardingTemplateId;
    private String logoUrl;
    private String primaryColor;
    private String secondaryColor;

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getJiraBaseUrl() {
        return jiraBaseUrl;
    }

    public void setJiraBaseUrl(String jiraBaseUrl) {
        this.jiraBaseUrl = jiraBaseUrl;
    }

    public String getSlackWorkspaceId() {
        return slackWorkspaceId;
    }

    public void setSlackWorkspaceId(String slackWorkspaceId) {
        this.slackWorkspaceId = slackWorkspaceId;
    }

    public String getGithubOrgId() {
        return githubOrgId;
    }

    public void setGithubOrgId(String githubOrgId) {
        this.githubOrgId = githubOrgId;
    }

    public String getGoogleWorkspaceId() {
        return googleWorkspaceId;
    }

    public void setGoogleWorkspaceId(String googleWorkspaceId) {
        this.googleWorkspaceId = googleWorkspaceId;
    }

    public String getOnboardingTemplateId() {
        return onboardingTemplateId;
    }

    public void setOnboardingTemplateId(String onboardingTemplateId) {
        this.onboardingTemplateId = onboardingTemplateId;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
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
} 