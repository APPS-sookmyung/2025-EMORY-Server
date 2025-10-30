package emory.emoryserver.calendar.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "events")
public class Event {
    @Id
    private String id;

    @Indexed
    private String userId;

    private String title;
    private String description;

    @Indexed
    private LocalDate date;
    private LocalTime startTime;

    private String eventType; // "USER_CREATED" | "GOOGLE_CALENDAR"
    private String googleEventId; // 구글 캘린더 연동시 사용

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}