package emory.emoryserver.calendar.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import emory.emoryserver.calendar.model.GoogleCalendarToken;
import emory.emoryserver.calendar.repository.GoogleCalendarTokenRepository;
import emory.emoryserver.global.config.GoogleCalendarConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final GoogleCalendarConfig config;
    private final NetHttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private final GoogleCalendarTokenRepository tokenRepository;

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    /**
     * OAuth 인증 URL 생성
     */
    public String getAuthorizationUrl(String userId) {
        try {
            GoogleAuthorizationCodeFlow flow = createFlow();
            return flow.newAuthorizationUrl()
                    .setRedirectUri(config.getRedirectUri())
                    .setState(userId) // userId를 state로 전달
                    .build();
        } catch (Exception e) {
            log.error("Failed to create authorization URL", e);
            throw new RuntimeException("구글 캘린더 인증 URL 생성 실패", e);
        }
    }

    /**
     * OAuth 콜백 처리 및 토큰 저장
     */
    public void handleOAuthCallback(String code, String userId) {
        try {
            GoogleAuthorizationCodeFlow flow = createFlow();
            TokenResponse tokenResponse = flow.newTokenRequest(code)
                    .setRedirectUri(config.getRedirectUri())
                    .execute();

            // 토큰 저장
            GoogleCalendarToken token = GoogleCalendarToken.builder()
                    .userId(userId)
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .expiresIn(tokenResponse.getExpiresInSeconds())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // 기존 토큰 삭제 후 저장
            tokenRepository.deleteByUserId(userId);
            tokenRepository.save(token);

            log.info("Google Calendar token saved for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to handle OAuth callback", e);
            throw new RuntimeException("구글 캘린더 토큰 저장 실패", e);
        }
    }

    /**
     * 사용자별 Calendar 서비스 생성
     */
    private Calendar getCalendarService(String userId) throws IOException {
        GoogleCalendarToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("구글 캘린더가 연동되지 않았습니다."));

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(config.getClientId(), config.getClientSecret())
                .build()
                .setAccessToken(token.getAccessToken())
                .setRefreshToken(token.getRefreshToken());

        return new Calendar.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(config.getApplicationName())
                .build();
    }

    /**
     * 특정 날짜의 구글 캘린더 이벤트 조회
     */
    public List<Event> getEventsByDate(String userId, LocalDate date) {
        try {
            Calendar service = getCalendarService(userId);

            com.google.api.client.util.DateTime startTime = toGoogleDateTime(date.atStartOfDay());
            com.google.api.client.util.DateTime endTime = toGoogleDateTime(date.plusDays(1).atStartOfDay());

            Events events = service.events().list("primary")
                    .setTimeMin(startTime)
                    .setTimeMax(endTime)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems() != null ? events.getItems() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch Google Calendar events", e);
            return Collections.emptyList();
        }
    }

    /**
     * 구글 캘린더에 이벤트 생성
     */
    public Event createEvent(String userId, String title, String description,
                             LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try {
            Calendar service = getCalendarService(userId);

            Event event = new Event()
                    .setSummary(title)
                    .setDescription(description);

            EventDateTime start = new EventDateTime()
                    .setDateTime(toGoogleDateTime(startDateTime))
                    .setTimeZone("Asia/Seoul");
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                    .setDateTime(toGoogleDateTime(endDateTime))
                    .setTimeZone("Asia/Seoul");
            event.setEnd(end);

            Event createdEvent = service.events().insert("primary", event).execute();
            log.info("Google Calendar event created: {}", createdEvent.getId());

            return createdEvent;
        } catch (Exception e) {
            log.error("Failed to create Google Calendar event", e);
            throw new RuntimeException("구글 캘린더 이벤트 생성 실패", e);
        }
    }

    /**
     * 구글 캘린더 이벤트 삭제
     */
    public void deleteEvent(String userId, String eventId) {
        try {
            Calendar service = getCalendarService(userId);
            service.events().delete("primary", eventId).execute();
            log.info("Google Calendar event deleted: {}", eventId);
        } catch (Exception e) {
            log.error("Failed to delete Google Calendar event", e);
            throw new RuntimeException("구글 캘린더 이벤트 삭제 실패", e);
        }
    }

    /**
     * 구글 캘린더 연동 해제
     */
    public void disconnectCalendar(String userId) {
        tokenRepository.deleteByUserId(userId);
        log.info("Google Calendar disconnected for user: {}", userId);
    }

    /**
     * 구글 캘린더 연동 여부 확인
     */
    public boolean isConnected(String userId) {
        return tokenRepository.findByUserId(userId).isPresent();
    }

    // Helper Methods

    private GoogleAuthorizationCodeFlow createFlow() throws IOException {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                .setClientId(config.getClientId())
                .setClientSecret(config.getClientSecret());

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
                .setInstalled(details);

        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, SCOPES)
                .setAccessType("offline")
                .build();
    }

    private com.google.api.client.util.DateTime toGoogleDateTime(LocalDateTime localDateTime) {
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return new com.google.api.client.util.DateTime(date);
    }
}