package emory.emoryserver.timecapsule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimecapsuleImageDto {
    @Schema(description = "일기 ID")
    private String diaryId;

    @Schema(description = "이미지 ID")
    private String imageId;

    @Schema(description = "일기 제목")
    private String title;

    @Schema(description = "날짜")
    private LocalDate date;
}