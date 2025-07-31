package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatMessageResponseDto {
    @Schema(description = "AI의 응답 메시지", example = "시험 보느라 고생 많았네요.")
    private String aiMessage;
}
