package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class DiarySaveRequestDto {
    private String diaryId;      // MongoDB ObjectId
   // private String imageId;     // AI가 생성한 이미지 URl
}