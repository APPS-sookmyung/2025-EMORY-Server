package emory.emoryserver.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateDetailResponseDto {
    @Schema(description = "날짜")
    private LocalDate date;
    @Schema(description = "일기 정보")
    private DiaryDetailDto diary;
    @Schema(description = "일정 목록")
    private List<EventResponseDto> events;
}