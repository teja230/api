package com.enterprise.agents.jira.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "jira_oauth_tokens")
@EqualsAndHashCode(callSuper = true)
public class JiraOAuthToken extends BaseOAuthToken {
    private String accountId;
    private String email;
    private String name;
    private String picture;
    private String siteUrl;
    private Integer expiresIn;

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }
}
