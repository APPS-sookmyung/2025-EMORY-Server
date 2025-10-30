package emory.emoryserver.calendar.controller;

import emory.emoryserver.calendar.dto.*;
import emory.emoryserver.calendar.service.CalendarService;
import emory.emoryserver.global.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Calendar", description = "달력 및 일정 관리 API")
@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final UserIdExtractor userIdExtractor;

    @Operation(summary = "월별 달력 데이터 조회")
    @GetMapping("/{year}/{month}")
    public CalendarResponseDto getMonthlyCalendar(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @RequestParam(name = "scrapedOnly", defaultValue = "false") boolean scrapedOnly,
            @AuthenticationPrincipal String email) {

        String userId = userIdExtractor.getUserIdFromEmail(email);

        if (scrapedOnly) {
            return calendarService.getScrapedCalendar(userId, year, month);
        } else {
            return calendarService.getMonthlyCalendar(userId, year, month);
        }
    }

    @Operation(summary = "특정 날짜 상세 정보 조회")
    @GetMapping("/date/{date}")
    public DateDetailResponseDto getDateDetail(
            @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", example = "2025-09-22")
            @PathVariable LocalDate date,
            @AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return calendarService.getDateDetail(userId, date);
    }

    @Operation(summary = "일정 등록")
    @PostMapping("/events")
    public EventResponseDto createEvent(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody EventRequestDto request) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return calendarService.createEvent(userId, request);
    }

    @Operation(summary = "일정 삭제")
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable String eventId,
            @AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        calendarService.deleteEvent(userId, eventId);
        return ResponseEntity.noContent().build();
    }
}