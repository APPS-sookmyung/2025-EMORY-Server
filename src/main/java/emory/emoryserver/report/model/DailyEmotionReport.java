package emory.emoryserver.report.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("daily_emotion_reports")
public class DailyEmotionReport {

    @Id
    private String id;

    private String userId;
    private LocalDate date;

    /**
     * 감정 분포(합 100) - key는 감정 라벨(예: HAPPY, SAD...)
     */
    private Map<String, Double> emotionDistribution;

    private String dominantEmotion;

    /**
     * 감정 분포 + 대화내용 기반 AI 피드백 (report 폴더 로직 재사용)
     */
    private String aiFeedback;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

