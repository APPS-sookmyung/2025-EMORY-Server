package emory.emoryserver.aidiary.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_diaries") // mongoDB 컬렉션명
public class AiDiary {
    @Id
    private String id;

    private String sessionId;
    private String userId;

    private String title;
    private String content;

    private String mood;
    private String imageId;

    private Integer version;  // ex: 1
    private String status;    // "DRAFT" | "FINAL"
    private Boolean editable; // true/false

    private LocalDate dateOfDay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<DiaryEdit> history;
}
