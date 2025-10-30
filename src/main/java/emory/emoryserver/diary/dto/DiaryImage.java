package emory.emoryserver.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryImage {
    @Schema(description = "일기 ID")
    private String diaryId;

    @Schema(description = "이미지 ID")
    private String imageId;

    @Schema(description = "일기 날짜", example = "2025-09-20")
    private LocalDate date;
}