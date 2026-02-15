package emory.emoryserver.ai.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ImageRequestDto {

    @Schema(description = "대화 세션 ID (권장)", example = "65b0c7a0f4c2a85c1a2b3c4d")
    private String sessionId;

    @Schema(description = "감정(옵션). 없으면 서비스에서 추정/기본 처리", example = "ANXIOUS")
    private String emotion;

    @Schema(description = "일기/요약 텍스트(옵션). sessionId 없이 생성하고 싶을 때 사용")
    private String diaryContent;
}
