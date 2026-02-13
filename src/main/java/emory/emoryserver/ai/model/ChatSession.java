package emory.emoryserver.ai.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Document("chat_sessions")
public class ChatSession {

    @Id
    private String id;              // = sessionId (UUID)

    private String userId;          // JWT subject(email) 권장

    private String selectedEmotion; // optional
    private String calendarSummary; // optional

    private String status;          // ACTIVE | STOPPED | SAVED

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime savedAt;
}
