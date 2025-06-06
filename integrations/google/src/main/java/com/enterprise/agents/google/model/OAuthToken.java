package com.enterprise.agents.google.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "google_oauth_tokens")
public class OAuthToken extends BaseOAuthToken {
    private String calendarId;
    private String email;
    private String name;
    private String picture;
    private String locale;
    private String timezone;
} 