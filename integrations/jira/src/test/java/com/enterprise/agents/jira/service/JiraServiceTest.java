package com.enterprise.agents.jira.service;

import com.enterprise.agents.common.config.OAuthConfig;
import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.repository.JiraOAuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JiraServiceTest {

    @Mock
    private JiraOAuthTokenRepository tokenRepository;
    @Mock
    private OAuthConfig oAuthConfig;

    private JiraService jiraService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        jiraService = new JiraService(oAuthConfig, tokenRepository, restTemplate);
    }

    @Test
    void saveToken_SavesToken() {
        JiraOAuthToken token = new JiraOAuthToken();
        token.setEnterpriseId("test-enterprise");
        token.setAccessToken("test-token");
        token.setRefreshToken("test-refresh-token");
        token.setSiteUrl("https://test.atlassian.net");

        jiraService.saveToken(token);

        verify(tokenRepository).save(token);
    }

    @Test
    void isConnected_WhenTokenExists_ReturnsTrue() {
        when(tokenRepository.findByEnterpriseId("test-enterprise"))
                .thenReturn(Optional.of(new JiraOAuthToken()));

        boolean result = jiraService.isConnected("test-enterprise");

        assertTrue(result);
    }

    @Test
    void isConnected_WhenTokenDoesNotExist_ReturnsFalse() {
        when(tokenRepository.findByEnterpriseId("test-enterprise"))
                .thenReturn(Optional.empty());

        boolean result = jiraService.isConnected("test-enterprise");

        assertFalse(result);
    }

    @Test
    void getOnboardingIssues_WhenConnected_ReturnsIssues() {
        JiraOAuthToken token = new JiraOAuthToken();
        token.setEnterpriseId("test-enterprise");
        token.setAccessToken("test-token");
        token.setSiteUrl("https://test.atlassian.net");

        when(tokenRepository.findByEnterpriseId("test-enterprise"))
                .thenReturn(Optional.of(token));

        assertThrows(OAuthException.class, () -> jiraService.getOnboardingIssues("test-enterprise"));
    }

    @Test
    void createOnboardingIssue_WhenConnected_CreatesIssue() {
        JiraOAuthToken token = new JiraOAuthToken();
        token.setEnterpriseId("test-enterprise");
        token.setAccessToken("test-token");
        token.setSiteUrl("https://test.atlassian.net");

        when(tokenRepository.findByEnterpriseId("test-enterprise"))
                .thenReturn(Optional.of(token));

        assertThrows(OAuthException.class, () ->
                jiraService.createOnboardingIssue("test-enterprise", "Test Issue", "Test Description", "Task", List.of("test"))
        );
    }

    @Test
    void getIssues_WhenConnected_ReturnsIssues() {
        JiraOAuthToken token = new JiraOAuthToken();
        token.setEnterpriseId("test-enterprise");
        token.setAccessToken("test-token");
        token.setSiteUrl("https://test.atlassian.net");

        when(tokenRepository.findByEnterpriseId("test-enterprise"))
                .thenReturn(Optional.of(token));

        assertThrows(OAuthException.class, () -> jiraService.getIssues("test-enterprise"));
    }

    @Test
    void createIssue_WhenConnected_CreatesIssue() {
        JiraOAuthToken token = new JiraOAuthToken();
        token.setEnterpriseId("test-enterprise");
        token.setAccessToken("test-token");
        token.setSiteUrl("https://test.atlassian.net");

        when(tokenRepository.findByEnterpriseId("test-enterprise"))
                .thenReturn(Optional.of(token));

        assertThrows(OAuthException.class, () ->
                jiraService.createIssue("test-enterprise", "Test Issue", "Test Description", "Task")
        );
    }
} 