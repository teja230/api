package com.enterprise.agents.jira.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class JiraService {
    private final JiraOAuthTokenRepository tokenRepository;

    public void saveToken(JiraOAuthToken token) {
        tokenRepository.save(token);
    }

    public boolean isConnected(String enterpriseId) {
        return tokenRepository.findByEnterpriseId(enterpriseId).isPresent();
    }

    public Map<String, Object> getProjects(String enterpriseId) {
        JiraOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "JIRA not connected"));

        try {
            JiraRestClient client = new AsynchronousJiraRestClientFactory()
                    .createWithBearerTokenAuthentication(
                            URI.create(token.getSiteUrl()),
                            token.getAccessToken()
                    );

            Iterable<Project> projects = client.getProjectClient().getAllProjects().claim();
            return Map.of(
                    "projects",
                    StreamSupport.stream(projects.spliterator(), false)
                            .map(this::convertProject)
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    private Map<String, Object> convertProject(Project project) {
        Map<String, Object> result = new HashMap<>();
        result.put("key", project.getKey());
        result.put("name", project.getName());
        result.put("description", project.getDescription());
        result.put("lead", project.getLead() != null ? project.getLead().getDisplayName() : null);
        result.put("projectTypeKey", project.getProjectTypeKey());
        result.put("projectCategory", project.getProjectCategory() != null ?
                project.getProjectCategory().getName() : null);
        return result;
    }
} 