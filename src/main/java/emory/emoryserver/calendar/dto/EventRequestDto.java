package emory.emoryserver.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto {
    @Schema(description = "일정 제목", required = true)
    private String title;
    @Schema(description = "일정 날짜", required = true)
    private LocalDate date;
    @Schema(description = "시작 시간", required = true)
    private LocalTime startTime;
    @Schema(description = "설명 (선택사항)")
    private String description;
}
