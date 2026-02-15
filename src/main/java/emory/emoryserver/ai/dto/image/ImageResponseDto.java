package emory.emoryserver.ai.dto.image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDto {
    private String imageUrl;
    private String dominantColor;

    // 추가: 저장된 이미지 id
    private String imageId;
}
