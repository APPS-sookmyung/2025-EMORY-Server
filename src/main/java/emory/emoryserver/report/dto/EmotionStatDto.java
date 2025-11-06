package emory.emoryserver.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionStatDto {
    @Schema(description = "감정 카테고리", example = "기쁨")
    private String emotion;

    @Schema(description = "개수")
    private Integer count;

    @Schema(description = "비율 (0-100)")
    private Double percentage;
}
