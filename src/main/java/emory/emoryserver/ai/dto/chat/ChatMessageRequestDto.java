package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChatMessageRequestDto {
    @Schema(description = "대화 세션 ID", example = "abc123-session")
    private String sessionId;

    @Schema(description = "사용자의 메시지", example = "오늘 너무 힘들었어")
    private String userMessage;
}
