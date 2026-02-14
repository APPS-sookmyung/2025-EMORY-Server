package emory.emoryserver.aidiary.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Document("diary_feedbacks")
public class AiDiaryFeedback {

    @Id
    private String id;

    private String diaryId;        // AiDiary._id (ObjectId string)
    private String userId;         // 내부 userId

    private String selectedOption; // "말투가 어색해요" 같은 선택형 피드백

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
