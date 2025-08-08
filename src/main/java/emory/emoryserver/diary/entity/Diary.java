package emory.emoryserver.diary.entity;

import emory.emoryserver.common.entity.BaseEntity;
import emory.emoryserver.common.enums.EmotionCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "diaries")
@Getter
@Setter
@NoArgsConstructor
public class Diary extends BaseEntity {
    @Id
    private String id; // diaryId
    private String userId;
    private LocalDate date;
    private String title;
    private String content;
    private EmotionCategory emotionCategory = EmotionCategory.SOSO;
    private String aiImageUrl; // AI 이미지 URL
    private boolean isScraped = false;
}