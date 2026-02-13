package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RealtimeClientSecretResponseDto {

    @Schema(description = "OpenAI Realtime ephemeral key", example = "ek_abc123...")
    private String clientSecret;

    @Schema(description = "만료 시각(UNIX seconds)", example = "1760000000")
    private Long expiresAt;
}
