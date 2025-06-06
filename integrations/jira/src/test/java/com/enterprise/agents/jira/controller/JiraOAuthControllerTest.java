package com.enterprise.agents.jira.controller;

import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.repository.JiraOAuthTokenRepository;
import com.enterprise.agents.jira.service.JiraService;
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
class JiraOAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JiraOAuthTokenRepository tokenRepository;

    @MockBean
    private JiraService jiraService;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
    }

    @Test
    void testInitiateOAuth() throws Exception {
        mockMvc.perform(get("/oauth/jira/initiate")
                        .param("companyId", "test-company"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://auth.atlassian.com/authorize?*"));
    }

    @Test
    void testInitiateOAuthWithoutCompanyId() throws Exception {
        mockMvc.perform(get("/oauth/jira/initiate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHandleCallback() throws Exception {
        // Given
        when(jiraService.exchangeCodeForToken(anyString(), anyString()))
                .thenReturn(new JiraOAuthToken());

        // When/Then
        mockMvc.perform(get("/oauth/jira/callback")
                        .param("code", "test-code")
                        .param("state", "test-company"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testHandleCallbackWithInvalidCode() throws Exception {
        // Given
        when(jiraService.exchangeCodeForToken(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid code"));

        // When/Then
        mockMvc.perform(get("/oauth/jira/callback")
                        .param("code", "invalid-code")
                        .param("state", "test-company"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testDisconnect() throws Exception {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(post("/oauth/jira/disconnect")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDisconnectNonExistentToken() throws Exception {
        mockMvc.perform(post("/oauth/jira/disconnect")
                        .param("companyId", "non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetConnectionStatus() throws Exception {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/oauth/jira/status")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true));
    }

    @Test
    void testGetConnectionStatusNotConnected() throws Exception {
        mockMvc.perform(get("/oauth/jira/status")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    void testRefreshToken() throws Exception {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("old-token");
        token.setRefreshToken("refresh-token");
        tokenRepository.save(token);

        when(jiraService.refreshToken(anyString()))
                .thenReturn(new JiraOAuthToken());

        // When/Then
        mockMvc.perform(post("/oauth/jira/refresh")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRefreshTokenWithoutRefreshToken() throws Exception {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("old-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(post("/oauth/jira/refresh")
                        .param("companyId", "test-company"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetProjects() throws Exception {
        // Given
        JiraOAuthToken token = new JiraOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/api/jira/projects")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetProjectsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/jira/projects")
                        .param("companyId", "test-company"))
                .andExpect(status().isUnauthorized());
    }
} 