package com.enterprise.agents.github.service;

import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.github.model.GitHubOAuthToken;
import com.enterprise.agents.github.repository.GitHubOAuthTokenRepository;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHOrganization;
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

    public List<Map<String, Object>> getRepositories(String companyId) {
        GitHubOAuthToken token = tokenRepository.findByEnterpriseId(companyId)
                .orElseThrow(() -> new OAuthException("not_connected", "GitHub not connected"));

        try {
            GitHub github = new GitHubBuilder()
                    .withOAuthToken(token.getAccessToken())
                    .build();

            return github.getMyself()
                    .getRepositories()
                    .values()
                    .stream()
                    .map(this::convertRepository)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getOrganizations(String companyId) {
        GitHubOAuthToken token = tokenRepository.findByEnterpriseId(companyId)
                .orElseThrow(() -> new OAuthException("not_connected", "GitHub not connected"));

        try {
            GitHub github = new GitHubBuilder()
                    .withOAuthToken(token.getAccessToken())
                    .build();

            return github.getMyself()
                    .getOrganizations()
                    .stream()
                    .map(org -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("id", org.getId());
                        result.put("login", org.getLogin());
                        result.put("url", org.getHtmlUrl().toString());
                        return result;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public Map<String, Object> createRepository(String companyId, String orgName, String repoName, String description, boolean isPrivate) {
        if (repoName == null || repoName.trim().isEmpty()) {
            return null;
        }

        GitHubOAuthToken token = tokenRepository.findByEnterpriseId(companyId)
                .orElseThrow(() -> new OAuthException("not_connected", "GitHub not connected"));

        try {
            GitHub github = new GitHubBuilder()
                    .withOAuthToken(token.getAccessToken())
                    .build();

            GHRepository repo = github.getOrganization(orgName)
                    .createRepository(repoName)
                    .description(description)
                    .private_(isPrivate)
                    .create();

            return convertRepository(repo);
        } catch (IOException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getTeams(String companyId, String orgName) {
        GitHubOAuthToken token = tokenRepository.findByEnterpriseId(companyId)
                .orElseThrow(() -> new OAuthException("not_connected", "GitHub not connected"));

        try {
            GitHub github = new GitHubBuilder()
                    .withOAuthToken(token.getAccessToken())
                    .build();

            return github.getOrganization(orgName)
                    .getTeams()
                    .values()
                    .stream()
                    .map(team -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("id", team.getId());
                        result.put("name", team.getName());
                        result.put("description", team.getDescription());
                        return result;
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public Map<String, Object> createTeam(String companyId, String orgName, String teamName, String description) {
        if (teamName == null || teamName.trim().isEmpty()) {
            return null;
        }

        GitHubOAuthToken token = tokenRepository.findByEnterpriseId(companyId)
                .orElseThrow(() -> new OAuthException("not_connected", "GitHub not connected"));

        try {
            GitHub github = new GitHubBuilder()
                    .withOAuthToken(token.getAccessToken())
                    .build();

            var team = github.getOrganization(orgName)
                    .createTeam(teamName)
                    .description(description)
                    .create();

            Map<String, Object> result = new HashMap<>();
            result.put("id", team.getId());
            result.put("name", team.getName());
            result.put("description", team.getDescription());
            return result;
        } catch (IOException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public boolean addTeamToRepository(String companyId, String orgName, String teamName, String repoName, String permission) {
        GitHubOAuthToken token = tokenRepository.findByEnterpriseId(companyId)
                .orElseThrow(() -> new OAuthException("not_connected", "GitHub not connected"));

        try {
            GitHub github = new GitHubBuilder()
                    .withOAuthToken(token.getAccessToken())
                    .build();

            var org = github.getOrganization(orgName);
            var team = org.getTeamByName(teamName);
            var repo = org.getRepository(repoName);

            team.add(repo, GHOrganization.Permission.valueOf(permission.toUpperCase()));
            return true;
        } catch (IOException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public GitHubOAuthToken exchangeCodeForToken(String code, String companyId) {
        var response = restTemplate.getForEntity(
                "https://github.com/login/oauth/access_token?client_id={clientId}&client_secret={clientSecret}&code={code}",
                String.class,
                "test-client-id",
                "test-client-secret",
                code
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String[] params = response.getBody().split("&");
            Map<String, String> tokenData = new HashMap<>();
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    tokenData.put(keyValue[0], keyValue[1]);
                }
            }

            GitHubOAuthToken token = new GitHubOAuthToken();
            token.setCompanyId(companyId);
            token.setAccessToken(tokenData.get("access_token"));
            token.setRefreshToken(tokenData.get("refresh_token"));
            return token;
        }

        throw new OAuthException("token_exchange_failed", "Failed to exchange code for token");
    }

    public void refreshToken(String companyId) {
        GitHubOAuthToken token = tokenRepository.findByEnterpriseId(companyId)
                .orElseThrow(() -> new OAuthException("not_connected", "GitHub not connected"));

        var response = restTemplate.getForEntity(
                "https://github.com/login/oauth/access_token?client_id={clientId}&client_secret={clientSecret}&refresh_token={refreshToken}",
                String.class,
                "test-client-id",
                "test-client-secret",
                token.getRefreshToken()
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String[] params = response.getBody().split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals("access_token")) {
                    token.setAccessToken(keyValue[1]);
                    tokenRepository.save(token);
                    return;
                }
            }
        }

        throw new OAuthException("token_refresh_failed", "Failed to refresh token");
    }

    private Map<String, Object> convertRepository(GHRepository repo) {
        Map<String, Object> result = new HashMap<>();
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
        return result;
    }
} 