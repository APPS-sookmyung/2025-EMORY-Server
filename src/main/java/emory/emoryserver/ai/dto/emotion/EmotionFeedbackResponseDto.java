package emory.emoryserver.ai.dto.emotion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmotionFeedbackResponseDto {
    @Schema(description = "사용자에게 전달할 위로 또는 피드백 메시지")
    private String message;
}
