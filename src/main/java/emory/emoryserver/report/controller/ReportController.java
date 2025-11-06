package emory.emoryserver.report.controller;

import emory.emoryserver.report.dto.ReportResponseDto;
import emory.emoryserver.report.service.ReportService;
import emory.emoryserver.global.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat; // import 추가
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth; // import 추가

@Tag(name = "Report", description = "주간/월간 감정 리포트 API")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserIdExtractor userIdExtractor;

    @Operation(summary = "주간 감정 리포트", description = "일요일부터 토요일 기준, 감정별 통계")
    @GetMapping("/weekly/{date}")
    public ReportResponseDto getWeeklyReport(
            @Parameter(description = "기준 날짜 (YYYY-MM-DD)")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return reportService.getWeeklyReport(userId, date);
    }

    @Operation(summary = "월간 감정 리포트")
    @GetMapping("/monthly/{yearMonth}") // URL 경로 수정
    public ReportResponseDto getMonthlyReport(
            @Parameter(description = "기준 월 (YYYY-MM)")
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return reportService.getMonthlyReport(userId, yearMonth);
    }
}