package emory.emoryserver.diary.dto;

import emory.emoryserver.common.enums.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "일기 응답")
public class DiaryResponse {
    @Schema(description = "일기 ID")
    private String id;

    @Schema(description = "일기 날짜")
    private LocalDate date;

    @Schema(description = "일기 제목")
    private String title;

    @Schema(description = "일기 내용")
    private String content;

    @Schema(description = "감정 카테고리")
    private EmotionCategory emotionCategory;

    @Schema(description = "AI 생성 대표 이미지")
    private String aiGeneratedImage;

    @Schema(description = "사용자 첨부 이미지")
    private List<String> userUploadedImages;

    @Schema(description = "스크랩 여부")
    private boolean isScraped;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
}
