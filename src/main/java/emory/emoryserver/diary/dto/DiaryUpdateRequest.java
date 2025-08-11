package emory.emoryserver.diary.dto;

import emory.emoryserver.common.enums.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Schema(description = "일기 수정 요청")
public class DiaryUpdateRequest {
    @Schema(description = "일기 제목")
    @NotBlank(message = "일기 제목은 필수입니다.")
    private String title;

    @Schema(description = "일기 내용")
    @NotBlank(message = "일기 내용은 필수입니다.")
    private String content;

    @Schema(description = "감정 카테고리")
    @NotNull(message = "감정 카테고리는 필수입니다.")
    private EmotionCategory emotionCategory;
}
