package emory.emoryserver.aidiary.controller;

import emory.emoryserver.aidiary.dto.DiarySaveRequestDto;
import emory.emoryserver.aidiary.dto.DiaryUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Diary Edit", description = "일기 작성 및 수정 API")
@RestController
@RequestMapping("/diary")
public class DiaryEditController {

    @Operation(summary = "AI 일기 저장", description = "AI가 최종적으로 생성한 일기, 이미지, 컬러를 저장합니다.")
    @PostMapping("/save")
    public void saveDiary(@RequestBody DiarySaveRequestDto request) {
        //일기 저장 로직
    }

    @Operation(summary = "AI 일기 수정", description = "사용자가 AI가 생성한 일기를 수정합니다.")
    @PutMapping("/edit/{diaryId}")
    public void updateDiary(@PathVariable long diaryId,
                            @RequestBody DiaryUpdateRequestDto request) {
        // 일기 수정 로직
    }

}
