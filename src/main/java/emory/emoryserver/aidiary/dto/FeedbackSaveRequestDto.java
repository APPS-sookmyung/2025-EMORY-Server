package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FeedbackSaveRequestDto {
    @Schema(description = "선택한 피드백 항목", example = "말투가 어색해요")
    private String selectedOption;
}
