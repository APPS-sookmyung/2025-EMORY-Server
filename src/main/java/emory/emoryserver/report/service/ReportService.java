package emory.emoryserver.report.service;

import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import emory.emoryserver.report.dto.EmotionStatDto;
import emory.emoryserver.report.dto.ReportResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AiDiaryRepository aiDiaryRepository;

    // ✅ 추가: GPT-5로 피드백 생성하는 서비스 (new file로 만들면 됨)
    private final OpenAIReportService openAIReportService;

    /**
     * 주간 리포트 생성 (일요일~토요일)
     */
    public ReportResponseDto getWeeklyReport(String userId, LocalDate date) {

        // 기준 날짜가 속한 주의 시작일(일요일)과 종료일(토요일) 계산
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEnd = weekStart.plusDays(6); // 토요일까지

        // 해당 주간의 모든 일기 조회 (감정이 있는 것만)
        List<AiDiary> weekDiaries =
                aiDiaryRepository.findByUserIdAndDateOfDayBetweenAndMoodIsNotNull(userId, weekStart, weekEnd);

        return buildReportResponse(weekDiaries, weekStart, weekEnd, "WEEKLY");
    }

    /**
     * 월간 리포트 생성
     */
    public ReportResponseDto getMonthlyReport(String userId, YearMonth yearMonth) {

        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        // 해당 월의 모든 일기 조회 (감정이 있는 것만)
        List<AiDiary> monthDiaries =
                aiDiaryRepository.findByUserIdAndDateOfDayBetweenAndMoodIsNotNull(userId, monthStart, monthEnd);

        return buildReportResponse(monthDiaries, monthStart, monthEnd, "MONTHLY");
    }

    /**
     * 리포트 응답 객체 생성 (+ aiFeedback 포함)
     */
    private ReportResponseDto buildReportResponse(
            List<AiDiary> diaries,
            LocalDate startDate,
            LocalDate endDate,
            String reportType
    ) {
        if (diaries.isEmpty()) {
            return ReportResponseDto.builder()
                    .periodStart(startDate)
                    .periodEnd(endDate)
                    .reportType(reportType)
                    .emotionStats(List.of())
                    .totalDiaryCount(0)
                    .dominantEmotion(null)
                    .aiFeedback(null) // ✅ 추가
                    .build();
        }

        // 1) 감정별 통계 계산
        Map<String, Long> emotionCounts = diaries.stream()
                .filter(diary -> diary.getMood() != null && !diary.getMood().isEmpty())
                .collect(Collectors.groupingBy(AiDiary::getMood, Collectors.counting()));

        int totalCount = emotionCounts.values().stream().mapToInt(Long::intValue).sum();

        // 2) 감정별 통계 DTO 생성
        List<EmotionStatDto> emotionStats = emotionCounts.entrySet().stream()
                .map(entry -> {
                    String emotion = entry.getKey();
                    Long count = entry.getValue();
                    Double percentage = totalCount > 0 ? (count.doubleValue() / totalCount) * 100 : 0.0;

                    return EmotionStatDto.builder()
                            .emotion(emotion)
                            .count(count.intValue())
                            .percentage(Math.round(percentage * 100.0) / 100.0) // 소수점 2자리
                            .build();
                })
                .sorted((a, b) -> b.getCount().compareTo(a.getCount())) // 개수 내림차순 정렬
                .collect(Collectors.toList());

        // 3) 가장 많은 감정 찾기
        String dominantEmotion = emotionStats.isEmpty() ? null : emotionStats.get(0).getEmotion();

        // 4) ✅ AI 피드백 생성 (대화/일기 내용 + 주요 감정 기반)
        // - 여기서는 "일기 content"를 대화요약으로 활용 (난이도 낮고 즉시 적용 가능)
        // - 너무 길면 비용/시간 늘어서 길이 제한 + 최대 3개만
        List<String> snippets = diaries.stream()
                .map(d -> {
                    String c = d.getContent() == null ? "" : d.getContent().trim();
                    if (c.length() > 300) c = c.substring(0, 300) + "…";
                    return c;
                })
                .filter(s -> !s.isBlank())
                .limit(3)
                .toList();

        String aiFeedback = null;
        try {
            aiFeedback = openAIReportService.generateFeedback(
                    dominantEmotion,
                    emotionStats,
                    snippets
            );
        } catch (Exception e) {
            // 피드백 생성 실패해도 리포트는 내려가게 (운영 안정성)
            aiFeedback = null;
        }

        return ReportResponseDto.builder()
                .periodStart(startDate)
                .periodEnd(endDate)
                .reportType(reportType)
                .emotionStats(emotionStats)
                .totalDiaryCount(diaries.size())
                .dominantEmotion(dominantEmotion)
                .aiFeedback(aiFeedback) // ✅ 추가
                .build();
    }

    /**
     * 현재 주차 계산 (일요일 기준)
     */
    public int getCurrentWeekOfYear() {
        LocalDate today = LocalDate.now();
        WeekFields weekFields = WeekFields.of(DayOfWeek.SUNDAY, 1);
        return today.get(weekFields.weekOfYear());
    }

    /**
     * 특정 날짜의 주차 계산 (일요일 기준)
     */
    public int getWeekOfYear(LocalDate date) {
        WeekFields weekFields = WeekFields.of(DayOfWeek.SUNDAY, 1);
        return date.get(weekFields.weekOfYear());
    }

    /**
     * 년도별 최대 주차 수 계산
     */
    public int getMaxWeekOfYear(int year) {
        LocalDate lastDayOfYear = LocalDate.of(year, 12, 31);
        WeekFields weekFields = WeekFields.of(DayOfWeek.SUNDAY, 1);
        return lastDayOfYear.get(weekFields.weekOfYear());
    }
}
