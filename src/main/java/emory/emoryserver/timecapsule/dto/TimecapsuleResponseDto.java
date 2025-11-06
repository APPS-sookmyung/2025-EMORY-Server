package emory.emoryserver.timecapsule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimecapsuleResponseDto {
    @Schema(description = "작년 같은 주 시작 날짜 (일요일)")
    private LocalDate weekStartDate;

    @Schema(description = "작년 같은 주 종료 날짜 (토요일)")
    private LocalDate weekEndDate;

    @Schema(description = "AI가 생성한 주간 요약")
    private String weekSummary;

    @Schema(description = "해당 주의 일기 이미지들")
    private List<TimecapsuleImageDto> images;

    @Schema(description = "일기 개수")
    private Integer diaryCount;
}
