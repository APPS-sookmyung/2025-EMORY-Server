package emory.emoryserver.ai.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Document("chat_messages")
public class ChatLog {
    @Id
    private String id;
    private String sessionId;
    private String userId;
    private String role;       // "user" | "assistant"
    private String text;       // 발화 내용
    private LocalDateTime createdAt;
}
