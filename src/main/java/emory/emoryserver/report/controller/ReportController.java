package emory.emoryserver.report.controller;

import emory.emoryserver.report.dto.ReportResponseDto;
import emory.emoryserver.report.service.ReportService;
import emory.emoryserver.global.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Report", description = "주간/월간 감정 리포트 API")
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final UserIdExtractor userIdExtractor;

    @Operation(summary = "주간 감정 리포트",
            description = "특정 주차의 감정 카테고리별 통계를 그래프 데이터로 제공합니다. 주차는 일요일부터 토요일까지를 기준으로 합니다.")
    @GetMapping("/weekly/{year}/{week}")
    public ReportResponseDto getWeeklyReport(
            @Parameter(description = "년도", example = "2025") @PathVariable Integer year,
            @Parameter(description = "주차 (1-53)", example = "38") @PathVariable Integer week,
            @AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return reportService.getWeeklyReport(userId, year, week);
    }

    @Operation(summary = "월간 감정 리포트",
            description = "특정 월의 감정 카테고리별 통계를 그래프 데이터로 제공합니다.")
    @GetMapping("/monthly/{year}/{month}")
    public ReportResponseDto getMonthlyReport(
            @Parameter(description = "년도", example = "2025") @PathVariable Integer year,
            @Parameter(description = "월 (1-12)", example = "9") @PathVariable Integer month,
            @AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return reportService.getMonthlyReport(userId, year, month);
    }
}