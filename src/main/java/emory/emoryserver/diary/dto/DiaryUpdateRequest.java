package emory.emoryserver.diary.dto;

import emory.emoryserver.common.enums.EmotionCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "일기 수정 요청")
public class DiaryUpdateRequest {
    @Schema(description = "일기 제목")
    private String title;

    @Schema(description = "일기 내용")
    private String content;

    @Schema(description = "감정 카테고리")
    private EmotionCategory emotionCategory;

    @Schema(description = "사용자 첨부 이미지 (최대 3개)")
    private List<String> userUploadedImages;
}