package emory.emoryserver.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryDetailDto {
    @Schema(description = "일기 ID")
    private String diaryId;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "내용")
    private String content;
    @Schema(description = "감정")
    private String emotion;
    @Schema(description = "AI 이미지 ID")
    private String imageId;
    @Schema(description = "스크랩 여부")
    private Boolean scraped;
}
