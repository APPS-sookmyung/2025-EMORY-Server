package emory.emoryserver.ai.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "ai_images")
public class AiGeneratedImage {

    @Id
    private String id;

    private String userId;
    private String sessionId;

    // OpenAI prompt (디버깅/재생성용)
    private String prompt;

    // 예: "png"
    private String format;

    // 순수 base64(앞에 data:image/... 붙지 않은)
    private String b64;

    private Instant createdAt;
}