package emory.emoryserver.aidiary.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackSaveResponseDto {
    private String feedbackId;
    private String diaryId;
    private String selectedOption;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
