package emory.emoryserver.aidiary.controller;

import emory.emoryserver.aidiary.dto.DiaryGenerateRequestDto;
import emory.emoryserver.aidiary.dto.DiaryGenerateResponseDto;
import emory.emoryserver.aidiary.service.AiDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/aidiary/diary")
@RequiredArgsConstructor
public class DiaryGenerateController {

    private final AiDiaryService aiDiaryService;

    @Operation(summary = "일기 초안 생성", description = "대화 로그를 바탕으로 일기 제목/내용 초안을 생성합니다.")
    @PostMapping("/generate")
    public DiaryGenerateResponseDto generate(@jakarta.validation.Valid @RequestBody DiaryGenerateRequestDto request,
                                             @RequestHeader("X-USER-ID") String userId) {
        return aiDiaryService.generateDiaryFromSession(request, userId);
    }

}
