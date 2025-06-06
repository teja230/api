package com.enterprise.agents.teams.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TeamsServiceTest {

    @Test
    void testSendMessageWithEmptyMessage() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        boolean result = teamsService.sendMessage("test-company", "channel-id", "");

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageWithSpecialCharacters() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        boolean result = teamsService.sendMessage("test-company", "channel-id", "Test@Message");

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageWithLongMessage() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        boolean result = teamsService.sendMessage("test-company", "channel-id", "a".repeat(4001));

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageWithInvalidChannel() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        boolean result = teamsService.sendMessage("test-company", "invalid-channel", "Test message");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannelsWithMalformedResponse() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<String> channels = teamsService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithMalformedResponse() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<String> users = teamsService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testGetChannelsWithNetworkTimeout() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<String> channels = teamsService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithNetworkTimeout() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<String> users = teamsService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testSendMessageWithNetworkTimeout() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        boolean result = teamsService.sendMessage("test-company", "channel-id", "Test message");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannelsWithQuotaExceeded() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        List<String> channels = teamsService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithQuotaExceeded() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        List<String> users = teamsService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testSendMessageWithInsufficientPermissions() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        boolean result = teamsService.sendMessage("test-company", "channel-id", "Test message");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannelsWithInsufficientPermissions() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        List<String> channels = teamsService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithInsufficientPermissions() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        List<String> users = teamsService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void testSendMessageWithServiceUnavailable() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        boolean result = teamsService.sendMessage("test-company", "channel-id", "Test message");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetChannelsWithServiceUnavailable() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        List<String> channels = teamsService.getChannels("test-company");

        // Then
        assertNotNull(channels);
        assertTrue(channels.isEmpty());
    }

    @Test
    void testGetUsersWithServiceUnavailable() {
        // Given
        TeamsOAuthToken token = new TeamsOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        List<String> users = teamsService.getUsers("test-company");

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
} 