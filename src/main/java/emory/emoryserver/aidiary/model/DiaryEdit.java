package emory.emoryserver.aidiary.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryEdit {
    private Integer version;
    private String title;
    private String content;
    private String mood;
    private List<String> tags;
    private LocalDateTime editedAt;
    private String editedBy; // "AI" 또는 "USER"

}
