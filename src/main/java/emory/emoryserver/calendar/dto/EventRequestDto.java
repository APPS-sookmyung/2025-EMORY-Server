package emory.emoryserver.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto {
    @Schema(description = "일정 제목", required = true)
    @NotBlank(message = "제목은 필수입니다")
    private String title;
    @Schema(description = "일정 날짜", required = true)
    @NotNull(message = "날짜는 필수입니다")
    private LocalDate date;
    @Schema(description = "시작 시간", required = true)
    @NotNull(message = "시작 시간은 필수입니다")
    private LocalTime startTime;
    @Schema(description = "설명 (선택사항)")
    private String description;
}
