package emory.emoryserver.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {
    @Schema(description = "일정 ID")
    private String eventId;
    @Schema(description = "제목")
    private String title;
    @Schema(description = "날짜")
    private LocalDate date;
    @Schema(description = "시작 시간")
    private LocalTime startTime;
    @Schema(description = "설명")
    private String description;
    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;
}
