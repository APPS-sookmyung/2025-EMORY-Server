package emory.emoryserver.calendar.controller;

import emory.emoryserver.calendar.service.GoogleCalendarService;
import emory.emoryserver.global.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

@Tag(name = "Google Calendar", description = "구글 캘린더 연동 API")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;
    private final UserIdExtractor userIdExtractor;

    @Operation(summary = "구글 캘린더 연동 시작", description = "OAuth 인증 URL을 반환합니다")
    @GetMapping("/google/connect")
    public ResponseEntity<Map<String, String>> connectGoogleCalendar(@AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        String authUrl = googleCalendarService.getAuthorizationUrl(userId);

        return ResponseEntity.ok(Map.of("authorizationUrl", authUrl));
    }

    @Operation(summary = "구글 캘린더 OAuth 콜백", description = "구글에서 리다이렉트되는 콜백 엔드포인트")
    @GetMapping("/oauth2/callback")
    public RedirectView handleOAuthCallback(
            @RequestParam String code,
            @RequestParam String state) { // state = userId

        googleCalendarService.handleOAuthCallback(code, state);

        // 프론트엔드 성공 페이지로 리다이렉트
        return new RedirectView("/calendar?connected=true");
    }

    @Operation(summary = "구글 캘린더 연동 해제")
    @DeleteMapping("/google/disconnect")
    public ResponseEntity<Void> disconnectGoogleCalendar(@AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        googleCalendarService.disconnectCalendar(userId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "구글 캘린더 연동 상태 확인")
    @GetMapping("/google/status")
    public ResponseEntity<Map<String, Boolean>> getConnectionStatus(@AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        boolean isConnected = googleCalendarService.isConnected(userId);

        return ResponseEntity.ok(Map.of("connected", isConnected));
    }
}
