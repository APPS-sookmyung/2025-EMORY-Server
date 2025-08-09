package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DiaryGenerateRequestDto {
    @Schema(description = "대화 세션 ID", example = "abc123-session")
    private String sessionId;

}
