package com.enterprise.agents.jira.service;

import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.common.model.CompanyConfig;
import com.enterprise.agents.common.service.CompanyConfigService;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.repository.JiraOAuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class JiraServiceTest {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private CompanyConfigService companyConfigService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private JiraOAuthTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();

        // Mock successful API responses
        when(restTemplate.exchange(
                eq("https://your-domain.atlassian.net/rest/api/2/search"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"issues\":[{\"id\":\"10001\",\"key\":\"PROJ-1\",\"fields\":{\"summary\":\"Test Issue\",\"description\":\"Test Description\",\"status\":{\"name\":\"To Do\"},\"assignee\":{\"displayName\":\"Test User\"}}}]}", HttpStatus.OK));

        when(restTemplate.exchange(
                eq("https://your-domain.atlassian.net/rest/api/2/issue"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"id\":\"10002\",\"key\":\"PROJ-2\"}", HttpStatus.CREATED));
    }

    @Test
    void testGetOnboardingIssues() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableJiraIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var issues = jiraService.getOnboardingIssues(companyId);

        // Then
        assertNotNull(issues);
        assertFalse(issues.isEmpty());
        assertEquals("PROJ-1", issues.get(0).getKey());
        assertEquals("Test Issue", issues.get(0).getSummary());
    }

    @Test
    void testCreateOnboardingIssue() {
        // Given
        String companyId = "test-company";
        String summary = "New Issue";
        String description = "New Description";
        String assignee = "test@example.com";
        List<String> labels = List.of("onboarding");

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableJiraIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var result = jiraService.createOnboardingIssue(companyId, summary, description, assignee, labels);

        // Then
        assertNotNull(result);
        assertEquals("PROJ-2", result.getKey());
    }

    @Test
    void testGetOnboardingIssuesWithoutToken() {
        // Given
        String companyId = "test-company";

        // When
        var issues = jiraService.getOnboardingIssues(companyId);

        // Then
        assertTrue(issues.isEmpty());
    }

    @Test
    void testCreateOnboardingIssueWithoutToken() {
        // Given
        String companyId = "test-company";
        String summary = "New Issue";
        String description = "New Description";
        String assignee = "test@example.com";
        List<String> labels = List.of("onboarding");

        // When
        var result = jiraService.createOnboardingIssue(companyId, summary, description, assignee, labels);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateIssueWithEmptySummary() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        String issueId = jiraService.createIssue("test-company", "", "Test description", "BUG");

        // Then
        assertNull(issueId);
    }

    @Test
    void testCreateIssueWithSpecialCharacters() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String issueId = jiraService.createIssue("test-company", "Test@Issue", "Test description", "BUG");

        // Then
        assertNull(issueId);
    }

    @Test
    void testCreateIssueWithLongSummary() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String issueId = jiraService.createIssue("test-company", "a".repeat(256), "Test description", "BUG");

        // Then
        assertNull(issueId);
    }

    @Test
    void testCreateIssueWithInvalidIssueType() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String issueId = jiraService.createIssue("test-company", "Test Issue", "Test description", "INVALID_TYPE");

        // Then
        assertNull(issueId);
    }

    @Test
    void testGetIssuesWithMalformedResponse() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<String> issues = jiraService.getIssues("test-company");

        // Then
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void testGetProjectsWithMalformedResponse() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<String> projects = jiraService.getProjects("test-company");

        // Then
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    void testGetIssuesWithNetworkTimeout() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<String> issues = jiraService.getIssues("test-company");

        // Then
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void testGetProjectsWithNetworkTimeout() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<String> projects = jiraService.getProjects("test-company");

        // Then
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    void testCreateIssueWithNetworkTimeout() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        String issueId = jiraService.createIssue("test-company", "Test Issue", "Test description", "BUG");

        // Then
        assertNull(issueId);
    }

    @Test
    void testGetIssuesWithQuotaExceeded() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        List<String> issues = jiraService.getIssues("test-company");

        // Then
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void testGetProjectsWithQuotaExceeded() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        List<String> projects = jiraService.getProjects("test-company");

        // Then
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    void testCreateIssueWithInsufficientPermissions() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        String issueId = jiraService.createIssue("test-company", "Test Issue", "Test description", "BUG");

        // Then
        assertNull(issueId);
    }

    @Test
    void testGetIssuesWithInsufficientPermissions() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        List<String> issues = jiraService.getIssues("test-company");

        // Then
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void testGetProjectsWithInsufficientPermissions() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        List<String> projects = jiraService.getProjects("test-company");

        // Then
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    void testCreateIssueWithServiceUnavailable() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        String issueId = jiraService.createIssue("test-company", "Test Issue", "Test description", "BUG");

        // Then
        assertNull(issueId);
    }

    @Test
    void testGetIssuesWithServiceUnavailable() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        List<String> issues = jiraService.getIssues("test-company");

        // Then
        assertNotNull(issues);
        assertTrue(issues.isEmpty());
    }

    @Test
    void testGetProjectsWithServiceUnavailable() {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        List<String> projects = jiraService.getProjects("test-company");

        // Then
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }
} 