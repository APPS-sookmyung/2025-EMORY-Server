package emory.emoryserver.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "일기 목록 응답")
public class DiaryListResponse {
    @Schema(description = "일기 목록")
    private List<DiaryResponse> diaries;

    @Schema(description = "오늘 일기 작성 가능 여부")
    private boolean canWriteToday;

    @Schema(description = "오늘 날짜")
    private String todayDate;
}
