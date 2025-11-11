package emory.emoryserver.aidiary.controller;

import emory.emoryserver.aidiary.dto.DiaryGenerateRequestDto;
import emory.emoryserver.aidiary.dto.DiaryGenerateResponseDto;
import emory.emoryserver.aidiary.service.AiDiaryService;
import emory.emoryserver.global.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/aidiary/diary")
@RequiredArgsConstructor
public class DiaryGenerateController {

    private final UserIdExtractor userIdExtractor;
    private final AiDiaryService aiDiaryService;

    @Operation(summary = "일기 초안 생성", description = "대화 로그를 바탕으로 일기 제목/내용 초안을 생성합니다.")
    @PostMapping("/generate")
    public DiaryGenerateResponseDto generate(
            @Valid @RequestBody DiaryGenerateRequestDto request,
            //@RequestHeader("X-USER-ID") String userId
            @AuthenticationPrincipal String email) {

        String userId = userIdExtractor.getUserIdFromEmail(email);
        return aiDiaryService.generateDiaryFromSession(request, userId);
    }

}

