package com.enterprise.agents.google;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GoogleCalendarService {
    private final OAuthTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public GoogleCalendarService(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
        this.restTemplate = new RestTemplate();
    }

    public List<Map<String, Object>> getUpcomingEvents(String calendarId, int days) {
        OAuthToken token = tokenRepository.findById(calendarId)
                .orElseThrow(() -> new RuntimeException("Calendar not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());

        String now = Instant.now().toString();
        String end = Instant.now().plus(days, ChronoUnit.DAYS).toString();
        String url = String.format(
                "https://www.googleapis.com/calendar/v3/calendars/%s/events?timeMin=%s&timeMax=%s&singleEvents=true&orderBy=startTime",
                URLEncoder.encode(token.getCalendarId(), StandardCharsets.UTF_8),
                URLEncoder.encode(now, StandardCharsets.UTF_8),
                URLEncoder.encode(end, StandardCharsets.UTF_8)
        );

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        JSONObject json = new JSONObject(response.getBody());
        JSONArray events = json.getJSONArray("items");
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            JSONObject start = event.getJSONObject("start");
            JSONObject end = event.getJSONObject("end");

            result.add(Map.of(
                    "id", event.getString("id"),
                    "summary", event.getString("summary"),
                    "start", start.getString("dateTime"),
                    "end", end.getString("dateTime"),
                    "description", event.optString("description", ""),
                    "location", event.optString("location", "")
            ));
        }

        return result;
    }

    public void createEvent(String calendarId, String summary, String description,
                            String startTime, String endTime, String location) {
        OAuthToken token = tokenRepository.findById(calendarId)
                .orElseThrow(() -> new RuntimeException("Calendar not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        headers.set("Content-Type", "application/json");

        JSONObject event = new JSONObject();
        event.put("summary", summary);
        event.put("description", description);
        event.put("location", location);

        JSONObject start = new JSONObject();
        start.put("dateTime", startTime);
        start.put("timeZone", "UTC");
        event.put("start", start);

        JSONObject end = new JSONObject();
        end.put("dateTime", endTime);
        end.put("timeZone", "UTC");
        event.put("end", end);

        String url = String.format(
                "https://www.googleapis.com/calendar/v3/calendars/%s/events",
                URLEncoder.encode(token.getCalendarId(), StandardCharsets.UTF_8)
        );

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(event.toString(), headers),
                String.class
        );
    }

    public void deleteEvent(String calendarId, String eventId) {
        OAuthToken token = tokenRepository.findById(calendarId)
                .orElseThrow(() -> new RuntimeException("Calendar not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());

        String url = String.format(
                "https://www.googleapis.com/calendar/v3/calendars/%s/events/%s",
                URLEncoder.encode(token.getCalendarId(), StandardCharsets.UTF_8),
                URLEncoder.encode(eventId, StandardCharsets.UTF_8)
        );

        restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        );
    }
} 