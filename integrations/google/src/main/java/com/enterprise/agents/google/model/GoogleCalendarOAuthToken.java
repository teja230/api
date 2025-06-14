package com.enterprise.agents.google.model;

import com.enterprise.agents.common.model.BaseOAuthToken;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity(name = "GoogleCalendarOAuthToken")
@Table(name = "google_calendar_oauth_tokens")
@EqualsAndHashCode(callSuper = true)
public class GoogleCalendarOAuthToken extends BaseOAuthToken {
    private String email;
    private String name;
    private String picture;
    private String timezone;
    private String calendarId;
} 