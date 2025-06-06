package com.enterprise.agents.jira.controller;

import com.enterprise.agents.common.config.MinimalTestApplication;
import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.jira.model.JiraOAuthToken;
import com.enterprise.agents.jira.repository.JiraOAuthTokenRepository;
import com.enterprise.agents.jira.service.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MinimalTestApplication.class)
@Import(TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class JiraOAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JiraOAuthTokenRepository tokenRepository;

    @MockBean
    private JiraService jiraService;

    @InjectMocks
    private JiraOAuthController jiraOAuthController;

    private JiraOAuthToken token;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        mockMvc = MockMvcBuilders.standaloneSetup(jiraOAuthController).build();
        token = new JiraOAuthToken();
        token.setEnterpriseId("test-enterprise");
        token.setAccessToken("test-token");
        token.setRefreshToken("test-refresh-token");
        token.setSiteUrl("https://test.atlassian.net");
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
    void handleCallback_WhenCodeProvided_ReturnsSuccess() throws Exception {
        when(jiraService.exchangeCodeForToken(anyString(), anyString())).thenReturn(token);

        mockMvc.perform(get("/api/jira/oauth/callback")
                        .param("code", "test-code")
                        .param("state", "test-state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(jiraService).exchangeCodeForToken("test-code", "test-state");
    }

    @Test
    void testDisconnect() throws Exception {
        mockMvc.perform(post("/oauth/jira/disconnect")
                        .param("enterpriseId", "test-enterprise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDisconnectNonExistentToken() throws Exception {
        mockMvc.perform(post("/oauth/jira/disconnect")
                        .param("enterpriseId", "non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetConnectionStatus() throws Exception {
        when(jiraService.isConnected(anyString())).thenReturn(true);

        mockMvc.perform(get("/oauth/jira/status")
                        .param("enterpriseId", "test-enterprise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true));
    }

    @Test
    void testGetConnectionStatusNotConnected() throws Exception {
        when(jiraService.isConnected(anyString())).thenReturn(false);

        mockMvc.perform(get("/oauth/jira/status")
                        .param("enterpriseId", "test-enterprise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    void refreshToken_WhenTokenExists_ReturnsSuccess() throws Exception {
        when(jiraService.refreshToken(anyString())).thenReturn(token);

        mockMvc.perform(post("/api/jira/oauth/refresh")
                        .param("enterpriseId", "test-enterprise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(jiraService).refreshToken("test-enterprise");
    }

    @Test
    void testGetProjects() throws Exception {
        when(jiraService.isConnected(anyString())).thenReturn(true);

        mockMvc.perform(get("/api/jira/projects")
                        .param("enterpriseId", "test-enterprise"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetProjectsWithoutToken() throws Exception {
        when(jiraService.isConnected(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/jira/projects")
                        .param("enterpriseId", "test-enterprise"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAuthUrl_ReturnsAuthUrl() throws Exception {
        mockMvc.perform(get("/api/v1/jira/auth/url")
                        .param("enterpriseId", "test-enterprise"))
                .andExpect(status().isOk());
    }

    @Test
    void handleCallback_WhenCodeProvided_ExchangesCodeForToken() throws Exception {
        when(jiraService.exchangeCodeForToken(any(), any())).thenReturn(token);

        mockMvc.perform(get("/api/v1/jira/auth/callback")
                        .param("code", "test-code")
                        .param("enterpriseId", "test-enterprise"))
                .andExpect(status().isOk());
    }

    @Test
    void refreshToken_WhenTokenExists_RefreshesToken() throws Exception {
        when(jiraService.refreshToken(any())).thenReturn(token);

        mockMvc.perform(post("/api/v1/jira/auth/refresh")
                        .param("enterpriseId", "test-enterprise")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
} 