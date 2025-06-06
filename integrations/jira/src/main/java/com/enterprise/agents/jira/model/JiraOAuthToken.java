package com.enterprise.agents.jira.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "jira_oauth_tokens")
public class JiraOAuthToken extends BaseOAuthToken {
    private String siteUrl;
    private String cloudId;
    private String accountId;
    private String accountType;
    private String accountStatus;
    private String name;
    private String picture;
    private String emailAddress;
    private String timeZone;
    private String locale;
    private String groups;
    private String applicationRoles;
    private String expand;
}
