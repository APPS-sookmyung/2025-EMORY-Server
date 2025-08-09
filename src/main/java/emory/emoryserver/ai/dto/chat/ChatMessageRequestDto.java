package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;


@Data
public class ChatMessageRequestDto {

    @Schema(description = "메시지 타입 (MESSAGE, FINISH_CHAT)")
    @NotNull(message = "type은 필수입니다.")
    private MessageType type;              // String → enum

    @Schema(description = "대화 세션 ID", example = "abc123-session")
    @NotBlank(message = "sessionId는 필수입니다.")
    private String sessionId;

    @Schema(description = "사용자 ID", example = "user-001")
    @NotBlank(message = "userId는 필수입니다.")
    private String userId;

    // MESSAGE일때만 필수 -> 핸들러에서 조건부 검사 유지
    private String userMessage;
}
