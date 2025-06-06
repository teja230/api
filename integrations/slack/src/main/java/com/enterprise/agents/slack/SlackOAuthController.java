package com.enterprise.agents.slack;

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
public class SlackOAuthController {
    private static final Logger logger = LoggerFactory.getLogger(SlackOAuthController.class);
    private final OAuthTokenRepository tokenRepository;
    @Value("${slack.oauth.client-id}")
    private String clientId;
    @Value("${slack.oauth.client-secret}")
    private String clientSecret;
    @Value("${slack.oauth.redirect-uri}")
    private String redirectUri;
    @Value("${slack.oauth.scopes:user.profile,channels:read,chat:write}")
    private String scopes;

    @Autowired
    public SlackOAuthController(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @GetMapping("/api/slack/oauth/url")
    public Map<String, String> getSlackOAuthUrl() {
        String url = "https://slack.com/oauth/v2/authorize?client_id=" + clientId +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return Map.of("url", url);
    }

    @GetMapping("/api/slack/status")
    public Map<String, Object> getSlackStatus() {
        boolean connected = tokenRepository.count() > 0;
        return Map.of("connected", connected);
    }

    @GetMapping("/api/slack/teams")
    public List<Map<String, String>> getConnectedTeams() {
        List<OAuthToken> tokens = tokenRepository.findAll();
        return tokens.stream()
                .map(t -> Map.of("teamId", t.getTeamId(), "teamName", t.getTeamName()))
                .toList();
    }

    @GetMapping("/api/slack/oauth/callback")
    public ResponseEntity<String> handleSlackCallback(@RequestParam String code) {
        try {
            String tokenUrl = "https://slack.com/api/oauth.v2.access" +
                    "?client_id=" + clientId +
                    "&client_secret=" + clientSecret +
                    "&code=" + code +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(tokenUrl, String.class);
            JSONObject json = new JSONObject(response);
            if (!json.optBoolean("ok")) {
                String error = json.optString("error", "Unknown error");
                logger.error("Slack OAuth error: {}", error);
                // Redirect to frontend with error using HTTP redirect
                String redirectUrl = "/success?status=error&msg=" + URLEncoder.encode(error, StandardCharsets.UTF_8);
                return ResponseEntity.status(302).header("Location", redirectUrl).build();
            }
            String accessToken = json.optString("access_token");
            JSONObject team = json.optJSONObject("team");
            String teamId = team != null ? team.optString("id") : "unknown";
            String teamName = team != null ? team.optString("name") : "unknown";
            OAuthToken token = new OAuthToken();
            token.setAccessToken(accessToken);
            token.setTeamId(teamId);
            token.setTeamName(teamName);
            tokenRepository.save(token);
            logger.info("Slack access token saved for team {} ({}).", teamName, teamId);
            // Redirect to frontend with success using HTTP redirect
            String redirectUrl = "/success?status=ok&team=" + URLEncoder.encode(teamName, StandardCharsets.UTF_8);
            return ResponseEntity.status(302).header("Location", redirectUrl).build();
        } catch (Exception e) {
            logger.error("Exception during Slack OAuth callback", e);
            String redirectUrl = "/success?status=error&msg=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(302).header("Location", redirectUrl).build();
        }
    }
}
