package emory.emoryserver.aidiary.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_diaries") // mongoDB 컬렉션명
@CompoundIndex(name = "uid_date_idx", def = "{'userId': 1, 'dateOfDay': -1}" )
public class AiDiary {
    @Id
    private String id;

    @Indexed
    private String sessionId;
    @Indexed
    private String userId;

    private String title;
    private String content;

    private String mood;
    private String imageId;

    @Builder.Default
    private Boolean scraped = false; // 스크랩

    @Builder.Default
    private Integer version = 1; //v1
    @Builder.Default
    private String status = "DRAFT";// "DRAFT" | "FINAL"
    @Builder.Default
    private Boolean editable = true; // true/false

    private LocalDate dateOfDay;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<DiaryEdit> history = new ArrayList<>();
}
