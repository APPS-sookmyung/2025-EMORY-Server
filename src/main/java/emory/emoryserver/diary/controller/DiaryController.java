package emory.emoryserver.diary.controller;

import emory.emoryserver.diary.dto.DiaryImage;
import emory.emoryserver.diary.dto.DiaryResponse;
import emory.emoryserver.diary.service.DiaryService;
import emory.emoryserver.global.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Diary", description = "최종 저장된 일기 조회, 삭제, 스크랩 API")
@RestController
@RequestMapping("/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final UserIdExtractor userIdExtractor;

    @Operation(summary = "전체 일기 목록 조회", description = "최종 저장 일기 최신순 조회")
    @GetMapping
    public List<DiaryResponse> getAllDiaries(@AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return diaryService.getAllDiaries(userId);
    }

    @Operation(summary = "사진 목록 조회 (최신순)", description = "이미지 최신순으로 조회")
    @GetMapping("/images")
    public List<DiaryImage> getDiaryImages(
            @AuthenticationPrincipal String email,
            @Parameter(description = "조회할 연도 (예: 2025)")
            @RequestParam(required = false) Integer year) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return diaryService.getDiaryImages(userId, year);
    }

    @Operation(summary = "일기 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(
            @PathVariable String id,
            @AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        diaryService.deleteDiary(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "스크랩 토글", description = "스크랩 on/off")
    @PatchMapping("/{id}/scrap")
    public DiaryResponse toggleScrap(
            @PathVariable String id,
            @AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return diaryService.toggleScrap(id, userId);
    }
}