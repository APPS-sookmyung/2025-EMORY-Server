package emory.emoryserver.report.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
public class DailyReportResponseDto {
    private LocalDate date;
    private String dominantEmotion;
    private Map<String, Double> emotionDistribution; // 합 100
    private List<EmotionStatDto> emotionStats;
    private String aiFeedback;
}
