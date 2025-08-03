package emory.emoryserver.diary.dto;

import emory.emoryserver.common.enums.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Schema(description = "일기 작성 요청 (AI 생성 후 사용자 수정 포함)")
public class DiaryCreateRequest {
    @Schema(description = "일기 날짜")
    private LocalDate date;

    @Schema(description = "일기 제목")
    private String title;

    @Schema(description = "일기 내용")
    private String content;

    @Schema(description = "감정 카테고리", defaultValue = "SOSO")
    private EmotionCategory emotionCategory = EmotionCategory.SOSO;

    @Schema(description = "AI 생성 대표 이미지")
    private String aiGeneratedImage;

    @Schema(description = "사용자 첨부 이미지 (최대 3개)")
    private List<String> userUploadedImages;
}