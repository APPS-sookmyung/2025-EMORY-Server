package emory.emoryserver.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "일기 이미지 응답")
public class DiaryImageResponse {
    @Schema(description = "일기 ID")
    private String id;

    @Schema(description = "일기 날짜")
    private LocalDate date;

    @Schema(description = "AI 이미지")
    private String aiImageUrl;
}
