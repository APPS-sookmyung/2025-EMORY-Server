package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ChatStartResponseDto {
    @Schema(description = "대화 세션 ID", example = "b7b1c2d7-6f4a-4c1c-9f0c-3d0f8d3a1b2c")
    private String sessionId;
}
