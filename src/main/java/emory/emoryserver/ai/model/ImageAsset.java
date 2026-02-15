package emory.emoryserver.ai.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "image_assets")
public class ImageAsset {

    @Id
    private String id;

    private String sessionId;

    /** image/png */
    private String mimeType;

    /** OpenAI가 준 base64 (raw, no data: prefix) */
    private String base64;

    /** 우리가 만든 프롬프트(디버깅용) */
    private String prompt;

    @CreatedDate
    private Instant createdAt;
}

