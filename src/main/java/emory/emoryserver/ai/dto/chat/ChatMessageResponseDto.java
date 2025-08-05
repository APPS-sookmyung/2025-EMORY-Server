package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatMessageResponseDto {
    private String sessionId;
    private String sender; //AI or USER
    private String message;
    private String timestamp;
}
