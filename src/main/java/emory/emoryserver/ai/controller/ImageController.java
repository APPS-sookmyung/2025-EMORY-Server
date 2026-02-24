package emory.emoryserver.ai.controller;

import emory.emoryserver.ai.dto.ImageGenerateRequest;
import emory.emoryserver.ai.service.AiImageService;
import emory.emoryserver.common.dto.ApiResponse;
import emory.emoryserver.global.util.UserIdExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/image")
@RequiredArgsConstructor
public class ImageController {

    private final AiImageService aiImageService;
    private final UserIdExtractor userIdExtractor;

    /**
     * 클라이언트는 sessionId만 전달합니다.
     * 서버가 해당 세션의 대화 로그를 바탕으로 이미지 프롬프트를 생성한 뒤, OpenAI Images API로 이미지를 생성합니다.
     */
    @PostMapping("/generate")
    public ApiResponse<String> generate(
            @AuthenticationPrincipal String email,
            @RequestBody ImageGenerateRequest request
    ) {
        String userId = userIdExtractor.extractUserId(email);
        String dataUri = aiImageService.generateImageFromSession(userId, request.sessionId());
        return ApiResponse.success(dataUri);
    }
}
