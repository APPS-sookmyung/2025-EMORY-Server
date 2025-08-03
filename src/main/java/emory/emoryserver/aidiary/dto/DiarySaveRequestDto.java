package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiarySaveRequestDto {
    @Schema(description = "AI가 생성한 일기 내용")
    private String diaryText;

    @Schema(description = "감정", example = "기쁨")
    private String emotion;

    @Schema(description = "일기 날짜", example = "2025-07-16")
    private LocalDate date;

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "대표 색상", example = "#A3C1DA")
    private String color;
}
