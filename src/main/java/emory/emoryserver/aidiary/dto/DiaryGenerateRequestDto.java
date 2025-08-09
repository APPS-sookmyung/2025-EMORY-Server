package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DiaryGenerateRequestDto {
    private List<String> chatLogs;
    private String sessionId;
    private String userId;
}
