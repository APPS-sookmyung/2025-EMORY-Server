package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ChatSaveRequestDto {

    @Schema(description = "세션 ID", example = "b7b1c2d7-6f4a-4c1c-9f0c-3d0f8d3a1b2c")
    private String sessionId;

    @Schema(description = "대화 로그(전사본) 배열", implementation = ChatTurnDto.class)
    private List<ChatTurnDto> messages;
}
