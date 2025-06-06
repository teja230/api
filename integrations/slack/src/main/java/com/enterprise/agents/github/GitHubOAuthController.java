package com.enterprise.agents.github;

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
public class GitHubOAuthController {
    private static final Logger logger = LoggerFactory.getLogger(GitHubOAuthController.class);
    private final OAuthTokenRepository tokenRepository;
    @Value("${github.oauth.client-id}")
    private String clientId;
    @Value("${github.oauth.client-secret}")
    private String clientSecret;
    @Value("${github.oauth.redirect-uri}")
    private String redirectUri;
    @Value("${github.oauth.scopes:repo,user,admin:org}")
    private String scopes;

    @Autowired
    public GitHubOAuthController(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @GetMapping("/api/github/oauth/url")
    public Map<String, String> getGitHubOAuthUrl() {
        String url = "https://github.com/login/oauth/authorize?client_id=" + clientId +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return Map.of("url", url);
    }

    @GetMapping("/api/github/status")
    public Map<String, Object> getGitHubStatus() {
        boolean connected = tokenRepository.count() > 0;
        return Map.of("connected", connected);
    }

    @GetMapping("/api/github/organizations")
    public List<Map<String, String>> getConnectedOrganizations() {
        List<OAuthToken> tokens = tokenRepository.findAll();
        return tokens.stream()
                .map(t -> Map.of("orgId", t.getOrgId(), "orgName", t.getOrgName()))
                .toList();
    }

    @GetMapping("/api/github/oauth/callback")
    public ResponseEntity<String> handleGitHubCallback(@RequestParam String code) {
        try {
            // Exchange code for access token
            String tokenUrl = "https://github.com/login/oauth/access_token";
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(
                    tokenUrl,
                    Map.of(
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "code", code,
                            "redirect_uri", redirectUri
                    ),
                    String.class
            );

            // Parse the response to get the access token
            String accessToken = response.split("&")[0].split("=")[1];

            // Get user organization info
            String userUrl = "https://api.github.com/user/orgs";
            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("Authorization", "Bearer " + accessToken);
                return execution.execute(request, body);
            });
            String orgResponse = restTemplate.getForObject(userUrl, String.class);
            JSONObject orgJson = new JSONObject(orgResponse);

            // Save the token and org info
            OAuthToken token = new OAuthToken();
            token.setAccessToken(accessToken);
            token.setOrgId(orgJson.optString("id"));
            token.setOrgName(orgJson.optString("login"));
            tokenRepository.save(token);

            logger.info("GitHub access token saved for organization {} ({}).",
                    token.getOrgName(), token.getOrgId());

            // Redirect to frontend with success
            String redirectUrl = "/success?status=ok&org=" +
                    URLEncoder.encode(token.getOrgName(), StandardCharsets.UTF_8);
            return ResponseEntity.status(302).header("Location", redirectUrl).build();

        } catch (Exception e) {
            logger.error("Exception during GitHub OAuth callback", e);
            String redirectUrl = "/success?status=error&msg=" +
                    URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(302).header("Location", redirectUrl).build();
        }
    }
} 