package emory.emoryserver.ai.dto.emotion;

import io.swagger.v3.oas.annotations.media.Schema;

public class EmotionFeedbackRequestDto {
    @Schema(description = "분석된 감정", example = "불안")
    private String emotion;

    @Schema(description = "AI가 추론한 감정 원인", example = "내일 시험이 너무 걱정돼서")
    private String reason;
}
