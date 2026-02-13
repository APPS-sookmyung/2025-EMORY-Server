package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatTurnDto {
    @Schema(description = "user | assistant", example = "user")
    private String role;

    @Schema(description = "발화 텍스트", example = "오늘 너무 힘들었어.")
    private String text;

    @Schema(description = "클라이언트에서 기록한 ISO 타임스탬프(선택)", example = "2026-02-13T12:34:56")
    private String timestamp; // optional (ISO)
}
