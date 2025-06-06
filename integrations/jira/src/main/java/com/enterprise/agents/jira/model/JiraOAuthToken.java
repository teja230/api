package com.enterprise.agents.jira.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "jira_oauth_tokens")
@EqualsAndHashCode(callSuper = true)
public class JiraOAuthToken extends BaseOAuthToken {
    private String siteUrl;
    private String accountId;
    private String displayName;
    private String email;
} 