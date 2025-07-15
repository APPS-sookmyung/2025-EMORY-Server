package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiaryResponseDto {
    @Schema(description = "AI가 생성한 일기 내용")
    private String diaryText;

    @Schema(description = "감정 태그", example = "슬픔")
    private String emotion;

    @Schema(description = "일기 생성 날짜", example = "2025-07-16")
    private LocalDate date;
}
