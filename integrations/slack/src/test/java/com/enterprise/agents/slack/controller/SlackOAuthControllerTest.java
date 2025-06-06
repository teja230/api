package com.enterprise.agents.slack.controller;

import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.slack.model.SlackOAuthToken;
import com.enterprise.agents.slack.repository.SlackOAuthTokenRepository;
import com.enterprise.agents.slack.service.SlackService;
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
class SlackOAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SlackOAuthTokenRepository tokenRepository;

    @MockBean
    private SlackService slackService;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
    }

    @Test
    void testInitiateOAuth() throws Exception {
        mockMvc.perform(get("/oauth/slack/initiate")
                        .param("companyId", "test-company"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://slack.com/oauth/v2/authorize?*"));
    }

    @Test
    void testInitiateOAuthWithoutCompanyId() throws Exception {
        mockMvc.perform(get("/oauth/slack/initiate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHandleCallback() throws Exception {
        // Given
        when(slackService.exchangeCodeForToken(anyString(), anyString()))
                .thenReturn(new SlackOAuthToken());

        // When/Then
        mockMvc.perform(get("/oauth/slack/callback")
                        .param("code", "test-code")
                        .param("state", "test-company"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testHandleCallbackWithInvalidCode() throws Exception {
        // Given
        when(slackService.exchangeCodeForToken(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid code"));

        // When/Then
        mockMvc.perform(get("/oauth/slack/callback")
                        .param("code", "invalid-code")
                        .param("state", "test-company"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testDisconnect() throws Exception {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(post("/oauth/slack/disconnect")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDisconnectNonExistentToken() throws Exception {
        mockMvc.perform(post("/oauth/slack/disconnect")
                        .param("companyId", "non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetConnectionStatus() throws Exception {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/oauth/slack/status")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true));
    }

    @Test
    void testGetConnectionStatusNotConnected() throws Exception {
        mockMvc.perform(get("/oauth/slack/status")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    void testRefreshToken() throws Exception {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("old-token");
        token.setRefreshToken("refresh-token");
        tokenRepository.save(token);

        when(slackService.refreshToken(anyString()))
                .thenReturn(new SlackOAuthToken());

        // When/Then
        mockMvc.perform(post("/oauth/slack/refresh")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRefreshTokenWithoutRefreshToken() throws Exception {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("old-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(post("/oauth/slack/refresh")
                        .param("companyId", "test-company"))
                .andExpect(status().isBadRequest());
    }
} 