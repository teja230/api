package com.enterprise.agents.controller;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class SlackOAuthController {

    private static final Logger logger = LoggerFactory.getLogger(SlackOAuthController.class);
    private final StringRedisTemplate redisTemplate;
    @Value("${slack.oauth.client-id}")
    private String clientId;
    @Value("${slack.oauth.client-secret}")
    private String clientSecret;
    @Value("${slack.oauth.redirect-uri}")
    private String redirectUri;
    @Value("${slack.oauth.scopes:user.profile,channels:read,chat:write}")
    private String scopes;
    @Value("${slack.oauth.token-expiration-seconds:3600}")
    private long tokenExpirationSeconds;

    @Autowired
    public SlackOAuthController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/api/slack/oauth/url")
    public Map<String, String> getSlackOAuthUrl() {
        String url = "https://slack.com/oauth/v2/authorize?client_id=" + clientId +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return Map.of("url", url);
    }

    @GetMapping("/api/slack/oauth/callback")
    public ResponseEntity<String> handleSlackCallback(@RequestParam String code) {
        String tokenUrl = "https://slack.com/api/oauth.v2.access" +
                "?client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(tokenUrl, String.class);
        JSONObject json = new JSONObject(response);
        if (json.optBoolean("ok")) {
            String accessToken = json.optString("access_token");
            JSONObject team = json.optJSONObject("team");
            String teamId = team != null ? team.optString("id") : "default";
            try {
                // Store in Redis with configurable expiration
                redisTemplate.opsForValue().set(
                        "slack:token:" + teamId,
                        accessToken,
                        java.time.Duration.ofSeconds(tokenExpirationSeconds)
                );
                logger.info("Stored Slack token for team {} in Redis with {} seconds expiration.", teamId, tokenExpirationSeconds);
            } catch (Exception e) {
                logger.error("Failed to store Slack token in Redis for team {}: {}", teamId, e.getMessage(), e);
                return ResponseEntity.status(500).body("Slack integration failed: Redis error.");
            }
        } else {
            logger.warn("Slack OAuth callback failed: {}", json.toString());
        }
        return ResponseEntity.ok("Slack integration successful! You may close this window.");
    }

    @GetMapping("/api/slack/token")
    public Map<String, String> getCachedToken(@RequestParam(defaultValue = "default") String teamId) {
        String token = null;
        try {
            token = redisTemplate.opsForValue().get("slack:token:" + teamId);
            logger.info("Fetched Slack token for team {} from Redis.", teamId);
        } catch (Exception e) {
            logger.error("Failed to fetch Slack token from Redis for team {}: {}", teamId, e.getMessage(), e);
        }
        return Map.of("teamId", teamId, "accessToken", token != null ? token : "");
    }

    @GetMapping("/api/slack/token/delete")
    public ResponseEntity<String> deleteToken(@RequestParam(defaultValue = "default") String teamId) {
        try {
            Boolean deleted = redisTemplate.delete("slack:token:" + teamId);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("Deleted Slack token for team {} from Redis.", teamId);
                return ResponseEntity.ok("Token deleted for team: " + teamId);
            } else {
                logger.warn("No token found to delete for team {}.", teamId);
                return ResponseEntity.status(404).body("No token found for team: " + teamId);
            }
        } catch (Exception e) {
            logger.error("Failed to delete Slack token from Redis for team {}: {}", teamId, e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete token for team: " + teamId);
        }
    }

    @GetMapping("/api/slack/redis/health")
    public ResponseEntity<String> checkRedisHealth() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            if ("PONG".equalsIgnoreCase(pong)) {
                logger.info("Redis health check successful.");
                return ResponseEntity.ok("Redis is healthy");
            } else {
                logger.warn("Redis health check failed: {}", pong);
                return ResponseEntity.status(500).body("Redis health check failed: " + pong);
            }
        } catch (Exception e) {
            logger.error("Redis health check error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Redis health check error: " + e.getMessage());
        }
    }
}
