package emory.emoryserver.report.service;

import emory.emoryserver.ai.model.ChatLog;
import emory.emoryserver.ai.repository.ChatLogRepository;
import emory.emoryserver.report.dto.EmotionStatDto;
import emory.emoryserver.report.dto.ReportResponseDto;
import emory.emoryserver.report.model.DailyEmotionReport;
import emory.emoryserver.report.repository.DailyEmotionReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final DailyEmotionReportRepository dailyEmotionReportRepository;
    private final ChatLogRepository chatLogRepository;
    private final OpenAIEmotionService openAIEmotionService;
    private final OpenAIReportService openAIReportService;

    /**
     * GET /report/daily/{date}
     * - 저장된 daily report 있으면 그대로 반환
     * - 없으면 해당 날짜 chat_messages 기반으로 생성 시도 후 저장/반환
     */
    public ReportResponseDto getDailyReport(String userId, LocalDate date) {
        Optional<DailyEmotionReport> saved = dailyEmotionReportRepository.findByUserIdAndDate(userId, date);
        if (saved.isPresent()) {
            return toResponse(saved.get());
        }

        // fallback: 해당 날짜의 chat_messages(userId 기준)로 생성
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        List<ChatLog> logs = chatLogRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(userId, start, end);
        if (logs == null || logs.isEmpty()) {
            return ReportResponseDto.builder()
                    .periodStart(date)
                    .periodEnd(date)
                    .reportType("DAILY")
                    .emotionStats(List.of())
                    .totalDiaryCount(0)
                    .dominantEmotion(null)
                    .aiFeedback(null)
                    .build();
        }

        DailyEmotionReport created = createAndSaveDailyReport(userId, date, logs);
        return toResponse(created);
    }

    /**
     * saveConversation 후처리 트리거용
     * - 전달받은 logs(세션 전체 로그)로 daily report 업서트
     */
    public void createOrUpdateDailyReportFromSession(String userId, LocalDate date, List<ChatLog> logs) {
        if (logs == null || logs.isEmpty()) return;

        Optional<DailyEmotionReport> existing = dailyEmotionReportRepository.findByUserIdAndDate(userId, date);

        DailyEmotionReport created = createDailyReportObject(userId, date, logs);

        if (existing.isPresent()) {
            DailyEmotionReport e = existing.get();
            e.setEmotionDistribution(created.getEmotionDistribution());
            e.setDominantEmotion(created.getDominantEmotion());
            e.setAiFeedback(created.getAiFeedback());
            e.setUpdatedAt(LocalDateTime.now());
            dailyEmotionReportRepository.save(e);
        } else {
            created.setCreatedAt(LocalDateTime.now());
            created.setUpdatedAt(LocalDateTime.now());
            dailyEmotionReportRepository.save(created);
        }
    }

    private DailyEmotionReport createAndSaveDailyReport(String userId, LocalDate date, List<ChatLog> logs) {
        DailyEmotionReport created = createDailyReportObject(userId, date, logs);
        created.setCreatedAt(LocalDateTime.now());
        created.setUpdatedAt(LocalDateTime.now());
        return dailyEmotionReportRepository.save(created);
    }

    private DailyEmotionReport createDailyReportObject(String userId, LocalDate date, List<ChatLog> logs) {
        // 1) OpenAI로 감정 분포 얻기 (합 100)
        Map<String, Double> dist = openAIEmotionService.analyzeDistribution(logs);

        // 2) dist -> emotionStats (응답에 필요)
        List<EmotionStatDto> emotionStats = toEmotionStats(dist);
        String dominantEmotion = emotionStats.isEmpty() ? null : emotionStats.get(0).getEmotion();

        // 3) 피드백 입력 snippets (user 발화 일부)
        List<String> snippets = buildSnippets(logs);

        // 4) 이미 구현된 report 폴더의 OpenAIReportService 재사용
        String aiFeedback;
        try {
            aiFeedback = openAIReportService.generateFeedback(dominantEmotion, emotionStats, snippets);
            if (aiFeedback != null && aiFeedback.isBlank()) aiFeedback = null;
        } catch (Exception e) {
            aiFeedback = null;
        }

        return DailyEmotionReport.builder()
                .userId(userId)
                .date(date)
                .emotionDistribution(dist)
                .dominantEmotion(dominantEmotion)
                .aiFeedback(aiFeedback)
                .build();
    }

    private List<EmotionStatDto> toEmotionStats(Map<String, Double> dist) {
        if (dist == null || dist.isEmpty()) return List.of();

        return dist.entrySet().stream()
                .map(e -> {
                    double pct = e.getValue() == null ? 0.0 : e.getValue();
                    pct = Math.round(pct * 100.0) / 100.0; // 소수점 2자리
                    return EmotionStatDto.builder()
                            .emotion(e.getKey())
                            .count((int) Math.round(pct)) // %를 정수로 넣음(프론트에서 count 안 써도 됨)
                            .percentage(pct)
                            .build();
                })
                .sorted((a, b) -> Double.compare(
                        b.getPercentage() == null ? 0.0 : b.getPercentage(),
                        a.getPercentage() == null ? 0.0 : a.getPercentage()
                ))
                .collect(Collectors.toList());
    }

    private List<String> buildSnippets(List<ChatLog> logs) {
        if (logs == null) return List.of();

        List<String> userTexts = new ArrayList<>();
        for (ChatLog log : logs) {
            if (log == null) continue;
            String role = log.getRole() == null ? "" : log.getRole().trim().toLowerCase();
            String text = log.getText() == null ? "" : log.getText().trim();
            if (!"user".equals(role)) continue;
            if (text.isBlank()) continue;

            if (text.length() > 300) text = text.substring(0, 300) + "…";
            userTexts.add(text);

            if (userTexts.size() >= 3) break;
        }
        return userTexts;
    }

    private ReportResponseDto toResponse(DailyEmotionReport report) {
        List<EmotionStatDto> emotionStats = toEmotionStats(report.getEmotionDistribution());

        return ReportResponseDto.builder()
                .periodStart(report.getDate())
                .periodEnd(report.getDate())
                .reportType("DAILY")
                .emotionStats(emotionStats)
                // daily는 “그날 리포트 1개 존재” 의미로 1, 없으면 0이 자연스러움
                .totalDiaryCount(1)
                .dominantEmotion(report.getDominantEmotion())
                .aiFeedback(report.getAiFeedback())
                .build();
    }
}

