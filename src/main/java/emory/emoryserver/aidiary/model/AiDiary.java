package emory.emoryserver.aidiary.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collation = "ai_diaries") // mongoDB 컬렉션명
public class AiDiary {
    @Id
    private String id;

    private String sessionId;
    private String userId;
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;
}
