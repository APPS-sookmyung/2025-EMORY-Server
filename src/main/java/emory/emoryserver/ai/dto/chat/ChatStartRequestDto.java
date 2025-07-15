package emory.emoryserver.ai.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jdk.jfr.DataAmount;


public class ChatStartRequestDto {

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "선택한 감정 카테고리", example = "슬픔")
    private String selectedEmotion;

    @Schema(description =  "해당 날짜의 캘린더 일정 요약",example = "시험 있는 날")
    private String calendarSummary;
}
