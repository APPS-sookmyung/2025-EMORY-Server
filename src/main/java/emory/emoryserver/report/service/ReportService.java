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
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AiDiaryRepository aiDiaryRepository;

    /**
     * 주간 리포트 생성 (일요일~토요일)
     */
    public ReportResponseDto getWeeklyReport(String userId, LocalDate date) {

        // 기준 날짜가 속한 주의 시작일(일요일)과 종료일(토요일) 계산
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEnd = weekStart.plusDays(6); // 토요일까지

        // 해당 주간의 모든 일기 조회 (감정이 있는 것만)
        List<AiDiary> weekDiaries = aiDiaryRepository.findByUserIdAndDateOfDayBetweenAndMoodIsNotNull(
                userId, weekStart, weekEnd);

        return buildReportResponse(weekDiaries, weekStart, weekEnd, "WEEKLY");
    }

    /**
     * 월간 리포트 생성
     */
    // 메서드 시그니처 수정 (year, month -> yearMonth)
    public ReportResponseDto getMonthlyReport(String userId, YearMonth yearMonth) {
        // YearMonth.of(year, month) 코드가 필요 없어짐
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        // 해당 월의 모든 일기 조회 (감정이 있는 것만)
        List<AiDiary> monthDiaries = aiDiaryRepository.findByUserIdAndDateOfDayBetweenAndMoodIsNotNull(
                userId, monthStart, monthEnd);

        return buildReportResponse(monthDiaries, monthStart, monthEnd, "MONTHLY");
    }

    /**
     * 리포트 응답 객체 생성
     */
    private ReportResponseDto buildReportResponse(List<AiDiary> diaries, LocalDate startDate, LocalDate endDate, String reportType) {
        if (diaries.isEmpty()) {
            return ReportResponseDto.builder()
                    .periodStart(startDate)
                    .periodEnd(endDate)
                    .reportType(reportType)
                    .emotionStats(List.of())
                    .totalDiaryCount(0)
                    .dominantEmotion(null)
                    .build();
        }

        // 감정별 통계 계산
        Map<String, Long> emotionCounts = diaries.stream()
                .filter(diary -> diary.getMood() != null && !diary.getMood().isEmpty())
                .collect(Collectors.groupingBy(AiDiary::getMood, Collectors.counting()));

        int totalCount = emotionCounts.values().stream().mapToInt(Long::intValue).sum();

        // 감정별 통계 DTO 생성
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

        // 가장 많은 감정 찾기
        String dominantEmotion = emotionStats.isEmpty() ? null : emotionStats.get(0).getEmotion();

        return ReportResponseDto.builder()
                .periodStart(startDate)
                .periodEnd(endDate)
                .reportType(reportType)
                .emotionStats(emotionStats)
                .totalDiaryCount(diaries.size())
                .dominantEmotion(dominantEmotion)
                .build();
    }

    /**
     * 현재 주차 계산 (일요일 기준)
     */
    public int getCurrentWeekOfYear() {
        LocalDate today = LocalDate.now();

        // 일요일을 주의 시작으로 하는 WeekFields 설정
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