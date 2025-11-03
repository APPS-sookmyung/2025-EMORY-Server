package emory.emoryserver.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDateDto {
    @Schema(description = "날짜")
    private LocalDate date;
    @Schema(description = "감정 카테고리", example = "기쁨")
    private String emotion;
    @Schema(description = "일기 존재 여부")
    private Boolean hasDiary;
    @Schema(description = "스크랩 여부")
    private Boolean isScraped;
}