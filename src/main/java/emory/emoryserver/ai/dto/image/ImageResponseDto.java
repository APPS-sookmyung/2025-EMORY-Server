package emory.emoryserver.ai.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ImageResponseDto {
    @Schema(description = "생성된 이미지 URL")
    private String imageUrl;

    @Schema(description = "대표 색상", example = "#FFDDC1")
    private String dominantColor;
}
