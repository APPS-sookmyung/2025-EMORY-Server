package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DiarySaveRequestDto {
    private String diaryId;      // MongoDB ObjectId
    private String content;      // 수정된 일기 내용
    private String imageId;     // AI가 생성한 이미지 URl
}