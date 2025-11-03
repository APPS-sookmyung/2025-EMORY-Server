package emory.emoryserver.calendar.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "google_calendar_tokens")
public class GoogleCalendarToken {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}