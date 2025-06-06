package com.enterprise.agents.google.controller;

import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.google.model.GoogleCalendarOAuthToken;
import com.enterprise.agents.google.repository.GoogleCalendarOAuthTokenRepository;
import com.enterprise.agents.google.service.GoogleCalendarService;
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
class GoogleCalendarOAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoogleCalendarOAuthTokenRepository tokenRepository;

    @MockBean
    private GoogleCalendarService calendarService;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
    }

    @Test
    void testInitiateOAuth() throws Exception {
        mockMvc.perform(get("/oauth/google/calendar/initiate")
                        .param("companyId", "test-company"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://accounts.google.com/o/oauth2/v2/auth?*"));
    }

    @Test
    void testInitiateOAuthWithoutCompanyId() throws Exception {
        mockMvc.perform(get("/oauth/google/calendar/initiate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testHandleCallback() throws Exception {
        // Given
        when(calendarService.exchangeCodeForToken(anyString(), anyString()))
                .thenReturn(new GoogleCalendarOAuthToken());

        // When/Then
        mockMvc.perform(get("/oauth/google/calendar/callback")
                        .param("code", "test-code")
                        .param("state", "test-company"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testHandleCallbackWithInvalidCode() throws Exception {
        // Given
        when(calendarService.exchangeCodeForToken(anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid code"));

        // When/Then
        mockMvc.perform(get("/oauth/google/calendar/callback")
                        .param("code", "invalid-code")
                        .param("state", "test-company"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testDisconnect() throws Exception {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(post("/oauth/google/calendar/disconnect")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testDisconnectNonExistentToken() throws Exception {
        mockMvc.perform(post("/oauth/google/calendar/disconnect")
                        .param("companyId", "non-existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetConnectionStatus() throws Exception {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/oauth/google/calendar/status")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true));
    }

    @Test
    void testGetConnectionStatusNotConnected() throws Exception {
        mockMvc.perform(get("/oauth/google/calendar/status")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false));
    }

    @Test
    void testRefreshToken() throws Exception {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("old-token");
        token.setRefreshToken("refresh-token");
        tokenRepository.save(token);

        when(calendarService.refreshToken(anyString()))
                .thenReturn(new GoogleCalendarOAuthToken());

        // When/Then
        mockMvc.perform(post("/oauth/google/calendar/refresh")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRefreshTokenWithoutRefreshToken() throws Exception {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("old-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(post("/oauth/google/calendar/refresh")
                        .param("companyId", "test-company"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCalendars() throws Exception {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/api/google/calendar/calendars")
                        .param("companyId", "test-company"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetCalendarsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/google/calendar/calendars")
                        .param("companyId", "test-company"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetEvents() throws Exception {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When/Then
        mockMvc.perform(get("/api/google/calendar/events")
                        .param("companyId", "test-company")
                        .param("calendarId", "primary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetEventsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/google/calendar/events")
                        .param("companyId", "test-company")
                        .param("calendarId", "primary"))
                .andExpect(status().isUnauthorized());
    }
} 