package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatMessageRequestDto {
    private String type;        // 메시지 타입 (e.g., FINISH_CHAT, 일반 메시지)
    private String sessionId;
    private String userId;
    private String userMessage;
}
