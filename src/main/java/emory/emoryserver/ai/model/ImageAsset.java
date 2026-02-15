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
@Document(collection = "images")
public class ImageAsset {
    @Id
    private String id;

    /**
     * base64 (no data-url prefix)
     */
    private String base64;

    /**
     * e.g. image/png
     */
    private String contentType;

    /**
     * e.g. "#A1B2C3"
     */
    private String dominantColor;

    private String sessionId;

    private Instant createdAt;
}
