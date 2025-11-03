package emory.emoryserver.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponseDto {
    @Schema(description = "년도")
    private Integer year;
    @Schema(description = "월")
    private Integer month;
    @Schema(description = "각 날짜별 데이터")
    private List<CalendarDateDto> dates;
}