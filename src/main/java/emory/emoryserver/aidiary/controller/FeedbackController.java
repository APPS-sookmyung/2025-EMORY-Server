package emory.emoryserver.aidiary.controller;

import emory.emoryserver.aidiary.dto.FeedbackSaveRequestDto;
import emory.emoryserver.aidiary.dto.FeedbackSaveResponseDto;
import emory.emoryserver.aidiary.service.FeedbackService;
import emory.emoryserver.global.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Diary Feedback", description = "AI 일기 피드백 저장 API")
@RestController
@RequestMapping("/aidiary/diary")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserIdExtractor userIdExtractor;

    @Operation(summary = "일기 피드백 저장/업데이트", description = "선택형 피드백을 저장합니다. 동일 diary에 재요청 시 업데이트됩니다.")
    @PostMapping("/{diaryId}/feedback")
    public FeedbackSaveResponseDto saveFeedback(
            @PathVariable String diaryId,
            @AuthenticationPrincipal String email,
            @Valid @RequestBody FeedbackSaveRequestDto request
    ) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return feedbackService.saveOrUpdate(diaryId, userId, request);
    }
}

