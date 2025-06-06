package com.enterprise.agents.slack.service;

import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.common.model.CompanyConfig;
import com.enterprise.agents.common.service.CompanyConfigService;
import com.enterprise.agents.slack.model.SlackOAuthToken;
import com.enterprise.agents.slack.repository.SlackOAuthTokenRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class SlackServiceTest {

    @Autowired
    private SlackService slackService;

    @Autowired
    private CompanyConfigService companyConfigService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private SlackOAuthTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();

        // Mock successful API responses
        when(restTemplate.exchange(
                eq("https://slack.com/api/conversations.list"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"ok\":true,\"channels\":[{\"id\":\"C123\",\"name\":\"general\"}]}", HttpStatus.OK));

        when(restTemplate.exchange(
                eq("https://slack.com/api/chat.postMessage"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK));
    }

    @Test
    void testSendMessage() {
        // Given
        String companyId = "test-company";
        String channel = "general";
        String message = "Test message";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableSlackNotifications(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        boolean result = slackService.sendMessage(companyId, channel, message);

        // Then
        assertTrue(result);
    }

    @Test
    void testSendMessageWithoutToken() {
        // Given
        String companyId = "test-company";
        String channel = "general";
        String message = "Test message";

        // When
        boolean result = slackService.sendMessage(companyId, channel, message);

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageWithInvalidToken() {
        // Given
        String companyId = "test-company";
        String channel = "general";
        String message = "Test message";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableSlackNotifications(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("invalid-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                eq("https://slack.com/api/chat.postMessage"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        boolean result = slackService.sendMessage(companyId, channel, message);

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageWithServerError() {
        // Given
        String companyId = "test-company";
        String channel = "general";
        String message = "Test message";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableSlackNotifications(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                eq("https://slack.com/api/chat.postMessage"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        boolean result = slackService.sendMessage(companyId, channel, message);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannels() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableSlackNotifications(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // Mock successful API response
        when(restTemplate.exchange(
                eq("https://slack.com/api/conversations.list"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"ok\":true,\"channels\":[{\"id\":\"C123\",\"name\":\"general\"}]}", HttpStatus.OK));

        // When
        var channels = slackService.getChannels(companyId);

        // Then
        assertNotNull(channels);
        assertFalse(channels.isEmpty());
        assertEquals("general", channels.get(0));
    }

    @Test
    void testGetChannelsWithoutToken() {
        // Given
        String companyId = "test-company";

        // When
        var channels = slackService.getChannels(companyId);

        // Then
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetChannelsWithInvalidToken() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableSlackNotifications(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("invalid-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                eq("https://slack.com/api/conversations.list"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        var channels = slackService.getChannels(companyId);

        // Then
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetChannelsWithServerError() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableSlackNotifications(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // Mock API error response
        when(restTemplate.exchange(
                eq("https://slack.com/api/conversations.list"),
                eq(org.springframework.http.HttpMethod.GET),
                any(),
                eq(String.class)
        )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        var channels = slackService.getChannels(companyId);

        // Then
        assertTrue(channels.isEmpty());
    }

    @Test
    void testExchangeCodeForToken() {
        // Given
        String code = "test-code";
        String companyId = "test-company";

        // Mock successful token exchange
        when(restTemplate.exchange(
                eq("https://slack.com/api/oauth.v2.access"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"ok\":true,\"access_token\":\"test-token\",\"refresh_token\":\"refresh-token\"}", HttpStatus.OK));

        // When
        SlackOAuthToken token = slackService.exchangeCodeForToken(code, companyId);

        // Then
        assertNotNull(token);
        assertEquals("test-token", token.getAccessToken());
        assertEquals("refresh-token", token.getRefreshToken());
        assertEquals(companyId, token.getCompanyId());
    }

    @Test
    void testExchangeCodeForTokenWithInvalidCode() {
        // Given
        String code = "invalid-code";
        String companyId = "test-company";

        // Mock error response
        when(restTemplate.exchange(
                eq("https://slack.com/api/oauth.v2.access"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When/Then
        assertThrows(RuntimeException.class, () -> slackService.exchangeCodeForToken(code, companyId));
    }

    @Test
    void testRefreshToken() {
        // Given
        String companyId = "test-company";
        String refreshToken = "refresh-token";

        // Setup OAuth token
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId(companyId);
        token.setRefreshToken(refreshToken);
        tokenRepository.save(token);

        // Mock successful token refresh
        when(restTemplate.exchange(
                eq("https://slack.com/api/oauth.v2.access"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("{\"ok\":true,\"access_token\":\"new-token\",\"refresh_token\":\"new-refresh-token\"}", HttpStatus.OK));

        // When
        SlackOAuthToken newToken = slackService.refreshToken(companyId);

        // Then
        assertNotNull(newToken);
        assertEquals("new-token", newToken.getAccessToken());
        assertEquals("new-refresh-token", newToken.getRefreshToken());
    }

    @Test
    void testRefreshTokenWithInvalidRefreshToken() {
        // Given
        String companyId = "test-company";
        String refreshToken = "invalid-refresh-token";

        // Setup OAuth token
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId(companyId);
        token.setRefreshToken(refreshToken);
        tokenRepository.save(token);

        // Mock error response
        when(restTemplate.exchange(
                eq("https://slack.com/api/oauth.v2.access"),
                eq(org.springframework.http.HttpMethod.POST),
                any(),
                eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When/Then
        assertThrows(RuntimeException.class, () -> slackService.refreshToken(companyId));
    }

    @Test
    void testSendMessageWithEmptyMessage() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        boolean result = slackService.sendMessage("test-company", "channel-id", "");

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageWithSpecialCharacters() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        boolean result = slackService.sendMessage("test-company", "channel-id", "Test@Message");

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageWithLongMessage() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        boolean result = slackService.sendMessage("test-company", "channel-id", "a".repeat(4001));

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageWithInvalidChannel() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        boolean result = slackService.sendMessage("test-company", "invalid-channel", "Test message");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannelsWithMalformedResponse() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<String> channels = slackService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithMalformedResponse() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<String> users = slackService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testGetChannelsWithNetworkTimeout() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<String> channels = slackService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithNetworkTimeout() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<String> users = slackService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testSendMessageWithNetworkTimeout() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        boolean result = slackService.sendMessage("test-company", "channel-id", "Test message");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannelsWithQuotaExceeded() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        List<String> channels = slackService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithQuotaExceeded() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        List<String> users = slackService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testSendMessageWithInsufficientPermissions() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        boolean result = slackService.sendMessage("test-company", "channel-id", "Test message");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannelsWithInsufficientPermissions() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        List<String> channels = slackService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithInsufficientPermissions() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        List<String> users = slackService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testSendMessageWithServiceUnavailable() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        boolean result = slackService.sendMessage("test-company", "channel-id", "Test message");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannelsWithServiceUnavailable() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        List<String> channels = slackService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithServiceUnavailable() {
        // Given
        SlackOAuthToken token = new SlackOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        List<String> users = slackService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
} 