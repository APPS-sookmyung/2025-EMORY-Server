package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DiaryUpdateRequestDto {
    @Schema(description = "수정된 일기 내용")
    private String updatedText;
}
