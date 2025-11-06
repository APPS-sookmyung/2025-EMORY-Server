package emory.emoryserver.timecapsule.controller;

import emory.emoryserver.timecapsule.dto.TimecapsuleResponseDto;
import emory.emoryserver.timecapsule.service.TimecapsuleService;
import emory.emoryserver.global.util.UserIdExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Timecapsule", description = "타임캡슐 API - 작년 같은 주의 일기 요약")
@RestController
@RequestMapping("/timecapsule")
@RequiredArgsConstructor
public class TimecapsuleController {

    private final TimecapsuleService timecapsuleService;
    private final UserIdExtractor userIdExtractor;

    @Operation(summary = "타임캡슐 조회",
            description = "작년 오늘과 같은 주(일~토)에 작성된 일기들을 AI가 요약하여 보여줍니다. 요약 내용과 함께 해당 주의 AI 이미지들도 함께 제공됩니다.")
    @GetMapping
    public TimecapsuleResponseDto getTimecapsule(@AuthenticationPrincipal String email) {
        String userId = userIdExtractor.getUserIdFromEmail(email);
        return timecapsuleService.getTimecapsule(userId);
    }
}