package com.enterprise.agents.github.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "github_oauth_tokens")
@EqualsAndHashCode(callSuper = true)
public class GitHubOAuthToken extends BaseOAuthToken {
    private String login;
    private String name;
    private String email;
    private String avatarUrl;
    private String organization;
} 