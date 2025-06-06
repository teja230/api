package com.enterprise.agents.github;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GitHubService {
    private final OAuthTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public GitHubService(OAuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
        this.restTemplate = new RestTemplate();
    }

    public List<Map<String, Object>> getRepositories(String orgId) {
        OAuthToken token = tokenRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        headers.set("Accept", "application/vnd.github.v3+json");

        String url = "https://api.github.com/orgs/" + token.getOrgName() + "/repos";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        JSONArray repos = new JSONArray(response.getBody());
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < repos.length(); i++) {
            JSONObject repo = repos.getJSONObject(i);
            result.add(Map.of(
                    "name", repo.getString("name"),
                    "description", repo.optString("description", ""),
                    "url", repo.getString("html_url"),
                    "private", repo.getBoolean("private")
            ));
        }

        return result;
    }

    public List<Map<String, Object>> getTeamMembers(String orgId) {
        OAuthToken token = tokenRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        headers.set("Accept", "application/vnd.github.v3+json");

        String url = "https://api.github.com/orgs/" + token.getOrgName() + "/members";
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        JSONArray members = new JSONArray(response.getBody());
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);
            result.add(Map.of(
                    "login", member.getString("login"),
                    "avatar_url", member.getString("avatar_url"),
                    "url", member.getString("html_url")
            ));
        }

        return result;
    }

    public void createRepository(String orgId, String name, String description, boolean isPrivate) {
        OAuthToken token = tokenRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        headers.set("Accept", "application/vnd.github.v3+json");

        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("description", description);
        body.put("private", isPrivate);
        body.put("auto_init", true);

        String url = "https://api.github.com/orgs/" + token.getOrgName() + "/repos";
        restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body.toString(), headers),
                String.class
        );
    }
} 