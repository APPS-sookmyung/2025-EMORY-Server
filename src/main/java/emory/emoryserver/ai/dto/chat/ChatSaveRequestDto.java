package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChatSaveRequestDto {
    @Schema(description = "세션 ID", example = "abc123-session")
    private String sessionId;

    @Schema(description = "전체 대화 내용", example = "사용자: 힘들어 / AI: 시험 보느라 고생 많았네요.")
    private String conversationLog;
}
