package emory.emoryserver.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    @Schema(description = "리포트 기간 시작")
    private LocalDate periodStart;

    @Schema(description = "리포트 기간 종료")
    private LocalDate periodEnd;

    @Schema(description = "리포트 타입", example = "WEEKLY")
    private String reportType; // "WEEKLY" | "MONTHLY"

    @Schema(description = "감정별 통계")
    private List<EmotionStatDto> emotionStats;

    @Schema(description = "총 일기 개수")
    private Integer totalDiaryCount;

    @Schema(description = "가장 많은 감정")
    private String dominantEmotion;
}