package emory.emoryserver.aidiary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaryUpdateRequestDto {
    private String content; //수정된 일기 내용
}

