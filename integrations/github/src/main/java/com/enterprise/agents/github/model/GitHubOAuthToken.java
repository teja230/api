package com.enterprise.agents.github.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity(name = "GitHubOAuthToken")
@Table(name = "github_oauth_tokens")
@EqualsAndHashCode(callSuper = true)
public class GitHubOAuthToken extends BaseOAuthToken {
    private String login;
    private String name;
    private String email;
    private String avatarUrl;
    private String organization;

    public String getCompanyId() {
        return this.getEnterpriseId();
    }

    public void setCompanyId(String companyId) {
        this.setEnterpriseId(companyId);
    }
} 