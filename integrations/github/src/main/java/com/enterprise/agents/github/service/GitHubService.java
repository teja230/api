package com.enterprise.agents.github.service;

import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.github.model.GitHubOAuthToken;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GitHubService {
    private final GitHubOAuthTokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    public void saveToken(GitHubOAuthToken token) {
        tokenRepository.save(token);
    }

    public boolean isConnected(String enterpriseId) {
        return tokenRepository.findByEnterpriseId(enterpriseId).isPresent();
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        var response = restTemplate.getForEntity(
                "https://api.github.com/user",
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }

        throw new OAuthException("api_error", "Failed to fetch user info");
    }

    public Map<String, Object> getRepositories(String enterpriseId) {
        GitHubOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "GitHub not connected"));

        try {
            GitHub github = new GitHubBuilder()
                    .withOAuthToken(token.getAccessToken())
                    .build();

            List<Map<String, Object>> repos = github.getMyself()
                    .getRepositories()
                    .values()
                    .stream()
                    .map(this::convertRepository)
                    .collect(Collectors.toList());

            return Map.of("repositories", repos);
        } catch (IOException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    private Map<String, Object> convertRepository(GHRepository repo) {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("id", repo.getId());
            result.put("name", repo.getName());
            result.put("fullName", repo.getFullName());
            result.put("description", repo.getDescription());
            result.put("url", repo.getHtmlUrl().toString());
            result.put("private", repo.isPrivate());
            result.put("fork", repo.isFork());
            result.put("stars", repo.getStargazersCount());
            result.put("forks", repo.getForksCount());
            result.put("language", repo.getLanguage());
        } catch (IOException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
        return result;
    }
} 