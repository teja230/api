package com.enterprise.agents.github.controller;

import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.github.model.GitHubOAuthToken;
import com.enterprise.agents.github.repository.GitHubOAuthTokenRepository;
import com.enterprise.agents.github.service.GitHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
class GitHubOAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GitHubOAuthTokenRepository tokenRepository;

    @MockBean
    private GitHubService githubService;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
    }

    @Test
    void testInitiateOAuth() throws Exception {
        mockMvc.perform(get("/oauth/github/initiate")
                        .param("companyId", "test-company"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://github.com/login/oauth/authorize?*"));
    }

    @Test
    void testInitiateOAuthWithoutCompanyId() throws Exception {
        mockMvc.perform(get("/oauth/github/initiate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHandleCallback() throws Exception {
        // Given
        when(githubService.exchangeCodeForToken(anyString(), anyString()))
                .thenReturn(new GitHubOAuthToken());

        // When/Then
        mockMvc.perform(get("/oauth/github/callback")
                        .param("code", "test-code")
                        .param("state", "test-company"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testHandleCallbackWithInvalidCode() throws Exception {
        // Given
        when(githubService.exchangeCodeForToken(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid code"));

        // When/Then
        mockMvc.perform(get("/oauth/github/callback")
                        .param("code", "invalid-code")
                        .param("state", "test-company"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testDisconnect() throws Exception {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(post("/oauth/github/disconnect")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDisconnectNonExistentToken() throws Exception {
        mockMvc.perform(post("/oauth/github/disconnect")
                        .param("companyId", "non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetConnectionStatus() throws Exception {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/oauth/github/status")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true));
    }

    @Test
    void testGetConnectionStatusNotConnected() throws Exception {
        mockMvc.perform(get("/oauth/github/status")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    void testRefreshToken() throws Exception {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("old-token");
        token.setRefreshToken("refresh-token");
        tokenRepository.save(token);

        when(githubService.refreshToken(anyString()))
                .thenReturn(new GitHubOAuthToken());

        // When/Then
        mockMvc.perform(post("/oauth/github/refresh")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRefreshTokenWithoutRefreshToken() throws Exception {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("old-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(post("/oauth/github/refresh")
                        .param("companyId", "test-company"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRepositories() throws Exception {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/api/github/repositories")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetRepositoriesWithoutToken() throws Exception {
        mockMvc.perform(get("/api/github/repositories")
                        .param("companyId", "test-company"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetOrganizations() throws Exception {
        // Given
        GitHubOAuthToken token = new GitHubOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/api/github/organizations")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetOrganizationsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/github/organizations")
                        .param("companyId", "test-company"))
                .andExpect(status().isUnauthorized());
    }
} 