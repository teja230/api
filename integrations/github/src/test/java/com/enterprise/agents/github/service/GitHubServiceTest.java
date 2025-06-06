package com.enterprise.agents.github.service;

import com.enterprise.agents.common.model.CompanyConfig;
import com.enterprise.agents.common.service.CompanyConfigService;
import com.enterprise.agents.github.model.GitHubOAuthToken;
import com.enterprise.agents.github.repository.GitHubOAuthTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class GitHubServiceTest {

    @Autowired
    private GitHubService githubService;

    @Autowired
    private CompanyConfigService companyConfigService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private GitHubOAuthTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();

        // Mock successful API responses
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenReturn(new ResponseEntity<>("[{\"id\":1,\"name\":\"test-repo\",\"full_name\":\"test-org/test-repo\",\"private\":false}]", HttpStatus.OK));

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenReturn(new ResponseEntity<>("[{\"login\":\"test-org\",\"id\":1}]", HttpStatus.OK));

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenReturn(new ResponseEntity<>("{\"id\":2,\"name\":\"new-repo\",\"full_name\":\"test-org/new-repo\"}", HttpStatus.CREATED));
    }

    @Test
    void testGetRepositories() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var repos = githubService.getRepositories(companyId);

        // Then
        assertNotNull(repos);
        assertFalse(repos.isEmpty());
        assertEquals("test-repo", repos.get(0).get("name"));
        assertEquals("test-org/test-repo", repos.get(0).get("fullName"));
    }

    @Test
    void testGetRepositoriesWithInvalidToken() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("invalid-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        var repos = githubService.getRepositories(companyId);

        // Then
        assertTrue(repos.isEmpty());
    }

    @Test
    void testGetRepositoriesWithRateLimit() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // Mock rate limit error
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        var repos = githubService.getRepositories(companyId);

        // Then
        assertTrue(repos.isEmpty());
    }

    @Test
    void testGetOrganizations() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var orgs = githubService.getOrganizations(companyId);

        // Then
        assertNotNull(orgs);
        assertFalse(orgs.isEmpty());
        assertEquals("test-org", orgs.get(0).get("login"));
    }

    @Test
    void testGetOrganizationsWithInvalidToken() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("invalid-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        var orgs = githubService.getOrganizations(companyId);

        // Then
        assertTrue(orgs.isEmpty());
    }

    @Test
    void testCreateRepository() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";
        String repoName = "new-repo";
        String description = "Test repository";
        boolean isPrivate = true;

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var result = githubService.createRepository(companyId, orgName, repoName, description, isPrivate);

        // Then
        assertNotNull(result);
        assertEquals("new-repo", result.get("name"));
        assertEquals("test-org/new-repo", result.get("fullName"));
    }

    @Test
    void testCreateRepositoryWithInvalidToken() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";
        String repoName = "new-repo";
        String description = "Test repository";
        boolean isPrivate = true;

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("invalid-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        var result = githubService.createRepository(companyId, orgName, repoName, description, isPrivate);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateRepositoryWithDuplicateName() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";
        String repoName = "existing-repo";
        String description = "Test repository";
        boolean isPrivate = true;

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY));

        // When
        var result = githubService.createRepository(companyId, orgName, repoName, description, isPrivate);

        // Then
        assertNull(result);
    }

    @Test
    void testExchangeCodeForToken() {
        // Given
        String code = "test-code";
        String companyId = "test-company";

        // When/Then
        assertDoesNotThrow(() -> githubService.exchangeCodeForToken(code, companyId));
    }

    @Test
    void testExchangeCodeForTokenWithInvalidCode() {
        // Given
        String code = "invalid-code";
        String companyId = "test-company";

        // Mock error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When/Then
        assertThrows(RuntimeException.class, () -> githubService.exchangeCodeForToken(code, companyId));
    }

    @Test
    void testRefreshToken() {
        // Given
        String companyId = "test-company";
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("old-token");
        token.setRefreshToken("refresh-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).getForEntity(anyString(), any(), any(), any(), any());

        // When/Then
        assertDoesNotThrow(() -> githubService.refreshToken(companyId));
    }

    @Test
    void testRefreshTokenWithInvalidRefreshToken() {
        // Given
        String companyId = "test-company";
        String refreshToken = "invalid-refresh-token";

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setRefreshToken(refreshToken);
        tokenRepository.save(token);

        // Mock error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When/Then
        assertThrows(RuntimeException.class, () -> githubService.refreshToken(companyId));
    }

    @Test
    void testGetTeams() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var teams = githubService.getTeams(companyId, orgName);

        // Then
        assertNotNull(teams);
        assertTrue(teams.isEmpty());
    }

    @Test
    void testGetTeamsWithInvalidToken() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("invalid-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        var teams = githubService.getTeams(companyId, orgName);

        // Then
        assertNotNull(teams);
        assertTrue(teams.isEmpty());
    }

    @Test
    void testCreateTeam() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";
        String teamName = "test-team";
        String description = "Test Team";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var result = githubService.createTeam(companyId, orgName, teamName, description);

        // Then
        assertNotNull(result);
        assertEquals("test-team", result.get("name"));
    }

    @Test
    void testCreateTeamWithInvalidToken() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";
        String teamName = "test-team";
        String description = "Test Team";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("invalid-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        var result = githubService.createTeam(companyId, orgName, teamName, description);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateTeamWithNameConflict() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";
        String teamName = "existing-team";
        String description = "Test Team";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any(Class.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY));

        // When
        var result = githubService.createTeam(companyId, orgName, teamName, description);

        // Then
        assertNull(result);
    }

    @Test
    void testAddTeamToRepository() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";
        String teamName = "test-team";
        String repoName = "test-repo";
        String permission = "admin";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).put(anyString(), any());

        // When
        boolean result = githubService.addTeamToRepository(companyId, orgName, teamName, repoName, permission);

        // Then
        assertTrue(result);
    }

    @Test
    void testAddTeamToRepositoryWithInvalidToken() {
        // Given
        String companyId = "test-company";
        String orgName = "test-org";
        String teamName = "test-team";
        String repoName = "test-repo";
        String permission = "admin";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGitHubIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("invalid-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).put(anyString(), any());

        // When
        boolean result = githubService.addTeamToRepository(companyId, orgName, teamName, repoName, permission);

        // Then
        assertFalse(result);
    }

    @Test
    void testAddTeamToRepositoryWithInvalidPermission() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).put(anyString(), any());

        // When
        boolean result = githubService.addTeamToRepository("test-company", "test-org", "test-team", "test-repo", "invalid-permission");

        // Then
        assertFalse(result);
    }

    @Test
    void testAddTeamToRepositoryWithNonExistentTeam() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).put(anyString(), any());

        // When
        boolean result = githubService.addTeamToRepository("test-company", "test-org", "non-existent-team", "test-repo", "admin");

        // Then
        assertFalse(result);
    }

    @Test
    void testAddTeamToRepositoryWithNonExistentRepository() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).put(anyString(), any());

        // When
        boolean result = githubService.addTeamToRepository("test-company", "test-org", "test-team", "non-existent-repo", "admin");

        // Then
        assertFalse(result);
    }

    @Test
    void testAddTeamToRepositoryWithInsufficientPermissions() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).put(anyString(), any());

        // When
        boolean result = githubService.addTeamToRepository("test-company", "test-org", "test-team", "test-repo", "admin");

        // Then
        assertFalse(result);
    }

    @Test
    void testAddTeamToRepositoryWithQuotaExceeded() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).put(anyString(), any());

        // When
        boolean result = githubService.addTeamToRepository("test-company", "test-org", "test-team", "test-repo", "admin");

        // Then
        assertFalse(result);
    }

    @Test
    void testAddTeamToRepositoryWithNetworkTimeout() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        doNothing().when(restTemplate).put(anyString(), any());

        // When
        boolean result = githubService.addTeamToRepository("test-company", "test-org", "test-team", "test-repo", "admin");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetRepositoriesWithMalformedResponse() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<Map<String, Object>> repositories = githubService.getRepositories("test-company");

        // Then
        assertNotNull(repositories);
        assertTrue(repositories.isEmpty());
    }

    @Test
    void testGetTeamsWithMalformedResponse() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<Map<String, Object>> teams = githubService.getTeams("test-company", "test-org");

        // Then
        assertNotNull(teams);
        assertTrue(teams.isEmpty());
    }

    @Test
    void testCreateRepositoryWithMalformedResponse() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn("invalid-json-response");

        // When
        Map<String, Object> result = githubService.createRepository("test-company", "test-repo", "Test Repository", "", true);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateTeamWithMalformedResponse() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn("invalid-json-response");

        // When
        Map<String, Object> result = githubService.createTeam("test-company", "test-org", "test-team", "Test Team");

        // Then
        assertNull(result);
    }

    @Test
    void testGetRepositoriesWithNetworkTimeout() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<Map<String, Object>> repositories = githubService.getRepositories("test-company");

        // Then
        assertNotNull(repositories);
        assertTrue(repositories.isEmpty());
    }

    @Test
    void testGetTeamsWithNetworkTimeout() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<Map<String, Object>> teams = githubService.getTeams("test-company", "test-org");

        // Then
        assertNotNull(teams);
        assertTrue(teams.isEmpty());
    }

    @Test
    void testCreateRepositoryWithNetworkTimeout() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        Map<String, Object> result = githubService.createRepository("test-company", "test-repo", "Test Repository", "", true);

        // Then
        assertNull(result);
    }

    @Test
    void testCreateTeamWithNetworkTimeout() {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        Map<String, Object> result = githubService.createTeam("test-company", "test-org", "test-team", "Test Team");

        // Then
        assertNull(result);
    }
} 