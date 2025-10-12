package emory.emoryserver.aidiary.controller;

import emory.emoryserver.aidiary.dto.DiaryGenerateResponseDto;
import emory.emoryserver.aidiary.dto.DiarySaveRequestDto;
import emory.emoryserver.aidiary.dto.DiaryUpdateRequestDto;
import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import emory.emoryserver.aidiary.service.AiDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Diary Edit", description = "일기 작성 및 수정 API")
// @RestController
@RequestMapping("/aidiary/diary")
// @RequiredArgsConstructor
public class DiaryEditController {

    private final AiDiaryService aiDiaryService;

    /** 일기 수정 */
    @Operation(summary = "일기 수정", description = "제목/내용/감정/이미지를 수정하고 버전 및 히스토리를 남깁니다.")
    @PatchMapping("/edit/{diaryId}")
    public DiaryGenerateResponseDto editDiary(
            @PathVariable String diaryId,
            @RequestHeader("X-USER-ID") String userId,
            @Valid @RequestBody DiaryUpdateRequestDto request) {
        return aiDiaryService.updateDiary(diaryId, userId, request);
    }

    // **최종 저장**
    @Operation(summary = "ai 일기 최종 저장", description = "상태를 final로 변경하고 더이상 편집할 수 없게 만듦")
    @PostMapping("/save")
    public DiaryGenerateResponseDto finalizeDiary(
            @RequestHeader("X-USER-ID") String userId,
            @Valid @RequestBody DiarySaveRequestDto request) {
        return aiDiaryService.finalizeDiary(request, userId);
    }

}