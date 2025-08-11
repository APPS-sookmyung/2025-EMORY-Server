package emory.emoryserver.diary.controller;

import emory.emoryserver.common.dto.ApiResponse;
import emory.emoryserver.diary.dto.DiaryCreateRequest;
import emory.emoryserver.diary.dto.DiaryImageResponse;
import emory.emoryserver.diary.dto.DiaryListResponse;
import emory.emoryserver.diary.dto.DiaryResponse;
import emory.emoryserver.diary.dto.DiaryUpdateRequest;
import emory.emoryserver.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diaries")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "일기 CRUD API")
public class DiaryController {
    private final DiaryService diaryService;

    @GetMapping
    @Operation(summary = "일기 목록 조회", description = "일기 목록을 카드 형태로 조회")
    public ResponseEntity<ApiResponse<DiaryListResponse>> getDiaryList(
            @Parameter(description = "사용자 ID") @RequestHeader("userId") String userId) {
        DiaryListResponse response = diaryService.getDiaryList(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/images")
    @Operation(summary = "일기 이미지 조회", description = "AI 생성 이미지만 조회 (스위치 ON)")
    public ResponseEntity<ApiResponse<List<DiaryImageResponse>>> getDiaryImages(
            @Parameter(description = "사용자 ID") @RequestHeader("userId") String userId) {
        List<DiaryImageResponse> response = diaryService.getDiaryImages(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "일기 작성", description = "AI 생성 후 사용자가 수정한 일기를 저장")
    public ResponseEntity<ApiResponse<DiaryResponse>> createDiary(
            @Parameter(description = "사용자 ID") @RequestHeader("userId") String userId,
            @Valid @RequestBody DiaryCreateRequest request) {
        DiaryResponse response = diaryService.createDiary(userId, request);
        return ResponseEntity.ok(ApiResponse.success("일기가 작성되었습니다.", response));
    }

    @PutMapping("/{diaryId}")
    @Operation(summary = "일기 수정", description = "카드 더블클릭 후 일기 수정")
    public ResponseEntity<ApiResponse<DiaryResponse>> updateDiary(
            @Parameter(description = "사용자 ID") @RequestHeader("userId") String userId,
            @Parameter(description = "일기 ID") @PathVariable String diaryId,
            @Valid @RequestBody DiaryUpdateRequest request) {
        DiaryResponse response = diaryService.updateDiary(userId, diaryId, request);
        return ResponseEntity.ok(ApiResponse.success("일기가 수정되었습니다.", response));
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "일기 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteDiary(
            @Parameter(description = "사용자 ID") @RequestHeader("userId") String userId,
            @Parameter(description = "일기 ID") @PathVariable String diaryId) {
        diaryService.deleteDiary(userId, diaryId);
        return ResponseEntity.ok(ApiResponse.success("일기가 삭제되었습니다.", null));
    }

    @PatchMapping("/{diaryId}/scrap")
    @Operation(summary = "일기 스크랩/언스크랩", description = "일기를 스크랩하거나 스크랩을 해제")
    public ResponseEntity<ApiResponse<DiaryResponse>> toggleScrap(
            @Parameter(description = "사용자 ID") @RequestHeader("userId") String userId,
            @Parameter(description = "일기 ID") @PathVariable String diaryId) {
        DiaryResponse response = diaryService.toggleScrap(userId, diaryId);
        return ResponseEntity.ok(ApiResponse.success("스크랩 상태가 변경되었습니다.", response));
    }
}