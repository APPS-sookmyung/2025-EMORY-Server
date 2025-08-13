package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DiaryGenerateRequestDto {
    private String sessionId;
    private LocalDate dateOfDay;
}
