package emory.emoryserver.aidiary.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collation = "ai_diaries") // mongoDB 컬렉션명
public class AiDiary {
    @Id
    private String id;

    private String sessionId;
    private String userId;
    private String content;

    // 이미지 URL 필드 추가
    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
