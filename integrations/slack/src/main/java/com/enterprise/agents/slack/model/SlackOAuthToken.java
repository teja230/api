package com.enterprise.agents.slack.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity(name = "SlackOAuthToken")
@Table(name = "slack_oauth_tokens")
@EqualsAndHashCode(callSuper = true)
public class SlackOAuthToken extends BaseOAuthToken {
    private String teamId;
    private String teamName;
    private String botUserId;
    private String botAccessToken;
    private String scope;

    public String getCompanyId() {
        return getEnterpriseId();
    }

    public void setCompanyId(String companyId) {
        setEnterpriseId(companyId);
    }
} 