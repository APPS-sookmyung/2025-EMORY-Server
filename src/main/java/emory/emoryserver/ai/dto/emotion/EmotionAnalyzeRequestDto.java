package emory.emoryserver.ai.dto.emotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmotionAnalyzeRequestDto {
    @Schema(description = "분석할 대화 내용", example = "오늘 하루 너무 지쳤어")
    private String conversationText;
}
