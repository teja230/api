package com.enterprise.agents.google.service;

import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.google.model.GoogleCalendarOAuthToken;
import com.enterprise.agents.google.repository.GoogleCalendarOAuthTokenRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {
    private final GoogleCalendarOAuthTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public void saveToken(GoogleCalendarOAuthToken token) {
        tokenRepository.save(token);
    }

    public boolean isConnected(String enterpriseId) {
        return tokenRepository.findByEnterpriseId(enterpriseId).isPresent();
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        var response = restTemplate.getForEntity(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }

        throw new OAuthException("api_error", "Failed to fetch user info");
    }

    public Map<String, Object> getEvents(String enterpriseId) {
        GoogleCalendarOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "Google Calendar not connected"));

        try {
            Calendar service = getCalendarService(token);
            String calendarId = token.getCalendarId() != null ? token.getCalendarId() : "primary";

            Instant now = Instant.now();
            DateTime timeMin = new DateTime(now.toString());
            DateTime timeMax = new DateTime(now.plus(7, ChronoUnit.DAYS).toString());
            Events events = service.events().list(calendarId)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Map<String, Object>> eventList = events.getItems().stream()
                    .map(this::convertEvent)
                    .collect(Collectors.toList());

            return Map.of("events", eventList);
        } catch (IOException | GeneralSecurityException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    private Calendar getCalendarService(GoogleCalendarOAuthToken token) throws GeneralSecurityException, IOException {
        Credential credential = new Credential(com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(token.getAccessToken());

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName("Enterprise Agents")
                .build();
    }

    private Map<String, Object> convertEvent(Event event) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", event.getId());
        result.put("summary", event.getSummary());
        result.put("description", event.getDescription());
        result.put("location", event.getLocation());
        result.put("start", event.getStart());
        result.put("end", event.getEnd());
        result.put("status", event.getStatus());
        result.put("htmlLink", event.getHtmlLink());
        return result;
    }
} 