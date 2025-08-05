package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatMessageRequestDto {
    private String sessionId;
    private String userId;
    private String userMessage;
}
