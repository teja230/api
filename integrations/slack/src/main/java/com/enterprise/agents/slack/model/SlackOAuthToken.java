package com.enterprise.agents.slack.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "slack_oauth_tokens")
@EqualsAndHashCode(callSuper = true)
public class SlackOAuthToken extends BaseOAuthToken {
    private String teamId;
    private String teamName;
    private String botUserId;
    private String botAccessToken;
} 