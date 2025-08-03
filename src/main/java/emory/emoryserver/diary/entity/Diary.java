package emory.emoryserver.diary.entity;

import emory.emoryserver.common.entity.BaseEntity;
import emory.emoryserver.common.enums.EmotionCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

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
    private String aiGeneratedImage; // 대표 사진
    private List<String> userUploadedImages; // 사용자 첨부 사진 (0~3개)
    private boolean isScraped = false;
}
