package emory.emoryserver.ai.dto.emotion;

import io.swagger.v3.oas.annotations.media.Schema;

public class EmotionVerifyRequestDto {
    @Schema(description = "사용자가 선택한 감정", example = "슬픔")
    private String selectedEmotion;

    @Schema(description = "AI가 분석할 텍스트", example = "하루 종일 마음이 가라앉았어")
    private String text;
}
