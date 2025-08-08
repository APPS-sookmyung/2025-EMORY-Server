package emory.emoryserver.diary.dto;

import emory.emoryserver.common.enums.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "일기 작성 요청 (AI 생성 후 사용자 수정 포함)")
public class DiaryCreateRequest {
    @Schema(description = "일기 날짜")
    @NotNull(message = "일기 날짜는 필수입니다.")
    private LocalDate date;

    @Schema(description = "일기 제목")
    @NotBlank(message = "일기 제목은 필수입니다.")
    private String title;

    @Schema(description = "일기 내용")
    @NotBlank(message = "일기 내용은 필수입니다.")
    private String content;

    @Schema(description = "감정 카테고리", defaultValue = "SOSO")
    private EmotionCategory emotionCategory = EmotionCategory.SOSO;

    @Schema(description = "AI 생성 대표 이미지 URL")
    private String aiImageUrl;
}