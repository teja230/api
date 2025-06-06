package com.enterprise.agents.google.service;

import com.enterprise.agents.common.config.TestConfig;
import com.enterprise.agents.common.model.CompanyConfig;
import com.enterprise.agents.common.service.CompanyConfigService;
import com.enterprise.agents.google.model.GoogleCalendarOAuthToken;
import com.enterprise.agents.google.repository.GoogleCalendarOAuthTokenRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class GoogleCalendarServiceTest {

    @Autowired
    private GoogleCalendarService calendarService;

    @Autowired
    private CompanyConfigService companyConfigService;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private GoogleCalendarOAuthTokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();

        // Mock successful API responses
        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any()
        )).thenReturn(new ResponseEntity<>("{\"items\":[{\"id\":\"event1\",\"summary\":\"Test Event\",\"start\":{\"dateTime\":\"2024-03-20T10:00:00Z\"},\"end\":{\"dateTime\":\"2024-03-20T11:00:00Z\"}}]}", HttpStatus.OK));

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any()
        )).thenReturn(new ResponseEntity<>("{\"items\":[{\"id\":\"primary\",\"summary\":\"Primary Calendar\"},{\"id\":\"work\",\"summary\":\"Work Calendar\"}]}", HttpStatus.OK));

        when(restTemplate.exchange(
                anyString(),
                any(),
                any(),
                any()
        )).thenReturn(new ResponseEntity<>("{\"id\":\"event2\",\"summary\":\"New Event\",\"start\":{\"dateTime\":\"2024-03-21T10:00:00Z\"},\"end\":{\"dateTime\":\"2024-03-21T11:00:00Z\"}}", HttpStatus.OK));
    }

    @Test
    void testGetEvents() {
        // Given
        String companyId = "test-company";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(7);

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGoogleCalendarIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var events = calendarService.getEvents(companyId, start, end);

        // Then
        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals("Test Event", events.get(0).getSummary());
    }

    @Test
    void testGetCalendars() {
        // Given
        String companyId = "test-company";

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGoogleCalendarIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var calendars = calendarService.getCalendars(companyId);

        // Then
        assertNotNull(calendars);
        assertFalse(calendars.isEmpty());
        assertEquals("Primary Calendar", calendars.get(0).getSummary());
    }

    @Test
    void testCreateEvent() {
        // Given
        String companyId = "test-company";
        String summary = "New Event";
        String description = "Test Description";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        List<String> attendees = List.of("test@example.com");

        // Setup company config
        CompanyConfig config = new CompanyConfig();
        config.setCompanyId(companyId);
        config.setCompanyName("Test Company");
        config.setEnableGoogleCalendarIntegration(true);
        companyConfigService.saveConfig(config);

        // Setup OAuth token
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId(companyId);
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        var result = calendarService.createEvent(companyId, summary, description, start, end, attendees);

        // Then
        assertNotNull(result);
        assertEquals("New Event", result.getSummary());
    }

    @Test
    void testGetEventsWithoutToken() {
        // Given
        String companyId = "test-company";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(7);

        // When
        var events = calendarService.getEvents(companyId, start, end);

        // Then
        assertTrue(events.isEmpty());
    }

    @Test
    void testGetCalendarsWithoutToken() {
        // Given
        String companyId = "test-company";

        // When
        var calendars = calendarService.getCalendars(companyId);

        // Then
        assertTrue(calendars.isEmpty());
    }

    @Test
    void testCreateEventWithoutToken() {
        // Given
        String companyId = "test-company";
        String summary = "New Event";
        String description = "Test Description";
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        List<String> attendees = List.of("test@example.com");

        // When
        var result = calendarService.createEvent(companyId, summary, description, start, end, attendees);

        // Then
        assertNull(result);
    }

    @Test
    void testExchangeCodeForToken() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setAccessToken("test-token");
        token.setRefreshToken("test-refresh-token");

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(token);

        // When
        GoogleCalendarOAuthToken result = calendarService.exchangeCodeForToken("test-code", "test-company");

        // Then
        assertNotNull(result);
        assertEquals("test-token", result.getAccessToken());
        assertEquals("test-refresh-token", result.getRefreshToken());
    }

    @Test
    void testExchangeCodeForTokenWithInvalidCode() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When/Then
        assertThrows(RuntimeException.class, () ->
                calendarService.exchangeCodeForToken("invalid-code", "test-company"));
    }

    @Test
    void testRefreshToken() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setAccessToken("new-token");
        token.setRefreshToken("new-refresh-token");

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(token);

        // When
        GoogleCalendarOAuthToken result = calendarService.refreshToken("old-refresh-token");

        // Then
        assertNotNull(result);
        assertEquals("new-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
    }

    @Test
    void testRefreshTokenWithInvalidToken() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When/Then
        assertThrows(RuntimeException.class, () ->
                calendarService.refreshToken("invalid-refresh-token"));
    }

    @Test
    void testGetCalendarsWithInvalidToken() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        List<String> calendars = calendarService.getCalendars("test-company");

        // Then
        assertNotNull(calendars);
        assertTrue(calendars.isEmpty());
    }

    @Test
    void testGetCalendarsWithServerError() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        List<String> calendars = calendarService.getCalendars("test-company");

        // Then
        assertNotNull(calendars);
        assertTrue(calendars.isEmpty());
    }

    @Test
    void testGetEventsWithInvalidToken() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        List<String> events = calendarService.getEvents("test-company", "primary");

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void testGetEventsWithServerError() {
        // Given
        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        List<String> events = calendarService.getEvents("test-company", "primary");

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void testCreateEventWithInvalidToken() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithServerError() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithInvalidDateTime() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "invalid-date", "invalid-date");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithPastDateTime() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2020-01-01T10:00:00", "2020-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithEndBeforeStart() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T11:00:00", "2024-01-01T10:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithNonExistentCalendar() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // When
        String eventId = calendarService.createEvent("test-company", "non-existent", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithInsufficientPermissions() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithQuotaExceeded() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithEmptyTitle() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithSpecialCharacters() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test@Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithLongTitle() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "a".repeat(1001), "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithInvalidTimeZone() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00+99:00", "2024-01-01T11:00:00+99:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithInvalidDateFormat() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "invalid-date", "invalid-date");

        // Then
        assertNull(eventId);
    }

    @Test
    void testCreateEventWithMalformedResponse() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn("invalid-json-response");

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testGetCalendarsWithMalformedResponse() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<String> calendars = calendarService.getCalendars("test-company");

        // Then
        assertNotNull(calendars);
        assertTrue(calendars.isEmpty());
    }

    @Test
    void testGetEventsWithMalformedResponse() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenReturn("invalid-json-response");

        // When
        List<String> events = calendarService.getEvents("test-company", "primary");

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void testGetCalendarsWithNetworkTimeout() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<String> calendars = calendarService.getCalendars("test-company");

        // Then
        assertNotNull(calendars);
        assertTrue(calendars.isEmpty());
    }

    @Test
    void testGetEventsWithNetworkTimeout() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        List<String> events = calendarService.getEvents("test-company", "primary");

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void testCreateEventWithNetworkTimeout() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testGetCalendarsWithQuotaExceeded() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        List<String> calendars = calendarService.getCalendars("test-company");

        // Then
        assertNotNull(calendars);
        assertTrue(calendars.isEmpty());
    }

    @Test
    void testGetEventsWithQuotaExceeded() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // When
        List<String> events = calendarService.getEvents("test-company", "primary");

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void testCreateEventWithInsufficientPermissions() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testGetCalendarsWithInsufficientPermissions() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        List<String> calendars = calendarService.getCalendars("test-company");

        // Then
        assertNotNull(calendars);
        assertTrue(calendars.isEmpty());
    }

    @Test
    void testGetEventsWithInsufficientPermissions() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        // When
        List<String> events = calendarService.getEvents("test-company", "primary");

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void testCreateEventWithServiceUnavailable() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.postForObject(anyString(), any(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        String eventId = calendarService.createEvent("test-company", "primary", "Test Event", "2024-01-01T10:00:00", "2024-01-01T11:00:00");

        // Then
        assertNull(eventId);
    }

    @Test
    void testGetCalendarsWithServiceUnavailable() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        List<String> calendars = calendarService.getCalendars("test-company");

        // Then
        assertNotNull(calendars);
        assertTrue(calendars.isEmpty());
    }

    @Test
    void testGetEventsWithServiceUnavailable() {
        // Given
        GoogleCalendarOAuthToken token = new GoogleCalendarOAuthToken();
        token.setCompanyId("test-company");
        token.setAccessToken("test-token");
        tokenRepository.save(token);

        when(restTemplate.getForObject(anyString(), any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

        // When
        List<String> events = calendarService.getEvents("test-company", "primary");

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }
} 