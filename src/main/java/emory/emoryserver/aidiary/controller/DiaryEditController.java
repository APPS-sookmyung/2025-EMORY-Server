package emory.emoryserver.aidiary.controller;

import emory.emoryserver.aidiary.dto.DiarySaveRequestDto;
import emory.emoryserver.aidiary.dto.DiaryUpdateRequestDto;
import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Diary Edit", description = "일기 작성 및 수정 API")
// @RestController
@RequestMapping("/diary")
public class DiaryEditController {
    private final AiDiaryRepository aiDiaryRepository;

    public DiaryEditController(AiDiaryRepository aiDiaryRepository) {
        this.aiDiaryRepository = aiDiaryRepository;
    }

    // **최종 저장**
    @Operation(summary = "AI 일기 저장", description = "AI가 최종적으로 생성한 일기, 이미지, 컬러를 저장합니다.")
    @PostMapping("/save")
    public ResponseEntity<AiDiary> saveDiary(@RequestBody DiarySaveRequestDto request) {
        AiDiary diary = aiDiaryRepository.findById(request.getDiaryId())
                .orElseThrow(() -> new RuntimeException("Diary not found"));

        diary.setContent(request.getContent());
        diary.setImageUrl(request.getImageUrl());
        diary.setUpdatedAt(LocalDateTime.now());

        AiDiary savedDiary = aiDiaryRepository.save(diary);
        return ResponseEntity.ok(savedDiary);
    }

    // **내용만 수정**
    @Operation(summary = "AI 일기 수정", description = "사용자가 AI가 생성한 일기를 수정합니다.")
    @PutMapping("/edit/{diaryId}")
    public ResponseEntity<AiDiary> updateDiary(@PathVariable String diaryId,
                                               @RequestBody DiaryUpdateRequestDto request) {
        AiDiary diary = aiDiaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Diary not found"));

        diary.setContent(request.getContent());
        diary.setUpdatedAt(LocalDateTime.now());

        AiDiary updatedDiary = aiDiaryRepository.save(diary);
        return ResponseEntity.ok(updatedDiary);
    }
}