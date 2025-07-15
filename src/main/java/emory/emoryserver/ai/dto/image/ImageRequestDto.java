package emory.emoryserver.ai.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;

public class ImageRequestDto {
    @Schema(description = "감정", example = "기쁨")
    private String emotion;

    @Schema(description = "일기 내용", example = "오늘은 친구들과 정말 즐거운 하루를 보냈어.")
    private String diaryContent;
}
