package com.enterprise.agents.google;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
public class GoogleCalendarOAuthController {
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarOAuthController.class);
    private final OAuthTokenRepository tokenRepository;
    @Value("${google.oauth.client-id}")
    private String clientId;
    @Value("${google.oauth.client-secret}")
    private String clientSecret;
    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;
    @Value("${google.oauth.scopes:https://www.googleapis.com/auth/calendar}")
    private String scopes;

    @Autowired
    public GoogleCalendarOAuthController(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @GetMapping("/api/google/oauth/url")
    public Map<String, String> getGoogleOAuthUrl() {
        String url = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&access_type=offline" +
                "&prompt=consent";
        return Map.of("url", url);
    }

    @GetMapping("/api/google/status")
    public Map<String, Object> getGoogleStatus() {
        boolean connected = tokenRepository.count() > 0;
        return Map.of("connected", connected);
    }

    @GetMapping("/api/google/calendars")
    public List<Map<String, String>> getConnectedCalendars() {
        List<OAuthToken> tokens = tokenRepository.findAll();
        return tokens.stream()
                .map(t -> Map.of("calendarId", t.getCalendarId(), "calendarName", t.getCalendarName()))
                .toList();
    }

    @GetMapping("/api/google/oauth/callback")
    public ResponseEntity<String> handleGoogleCallback(@RequestParam String code) {
        try {
            // Exchange code for access token
            String tokenUrl = "https://oauth2.googleapis.com/token";
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(
                    tokenUrl,
                    Map.of(
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "code", code,
                            "redirect_uri", redirectUri,
                            "grant_type", "authorization_code"
                    ),
                    String.class
            );

            JSONObject tokenJson = new JSONObject(response);
            String accessToken = tokenJson.getString("access_token");
            String refreshToken = tokenJson.getString("refresh_token");

            // Get calendar info
            String calendarUrl = "https://www.googleapis.com/calendar/v3/users/me/calendarList/primary";
            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("Authorization", "Bearer " + accessToken);
                return execution.execute(request, body);
            });
            String calendarResponse = restTemplate.getForObject(calendarUrl, String.class);
            JSONObject calendarJson = new JSONObject(calendarResponse);

            // Save the token and calendar info
            OAuthToken token = new OAuthToken();
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setCalendarId(calendarJson.getString("id"));
            token.setCalendarName(calendarJson.getString("summary"));
            tokenRepository.save(token);

            logger.info("Google Calendar access token saved for calendar {} ({}).",
                    token.getCalendarName(), token.getCalendarId());

            // Redirect to frontend with success
            String redirectUrl = "/success?status=ok&calendar=" +
                    URLEncoder.encode(token.getCalendarName(), StandardCharsets.UTF_8);
            return ResponseEntity.status(302).header("Location", redirectUrl).build();

        } catch (Exception e) {
            logger.error("Exception during Google Calendar OAuth callback", e);
            String redirectUrl = "/success?status=error&msg=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(302).header("Location", redirectUrl).build();
        }
    }
} 