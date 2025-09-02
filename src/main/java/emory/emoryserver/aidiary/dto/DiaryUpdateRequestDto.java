package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class DiaryUpdateRequestDto {
    @Schema(description = "수정할 제목", example = "너무 힘들었던 하루")
    private String title;          // null이면 유지

    @Schema(description = "수정할 내용")
    private String content;        // null이면 유지

    @Schema(description = "수정할 감정", example = "슬픔")
    private String mood;           // null이면 유지

    @Schema(description = "수정할 이미지 ID")
    private String imageId;        // null이면 유지

    @Schema(description = "낙관적 락을 위한 기대 버전", example = "1")
    private Integer expectedVersion; // 선택(보내면 버전충돌 방지)
}

