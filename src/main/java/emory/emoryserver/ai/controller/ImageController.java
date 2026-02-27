package emory.emoryserver.ai.controller;

import emory.emoryserver.ai.dto.ImageGenerateRequest;
import emory.emoryserver.ai.dto.image.ImageGenerateResultResponse;
import emory.emoryserver.ai.model.AiGeneratedImage;
import emory.emoryserver.ai.service.AiImageService;
import emory.emoryserver.common.dto.ApiResponse;
import emory.emoryserver.global.util.UserIdExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/ai/image")
@RequiredArgsConstructor
public class ImageController {

    private final AiImageService aiImageService;
    private final UserIdExtractor userIdExtractor;

    /**
     * (인증 필요) sessionId 기반으로 이미지 생성 → DB 저장 → imageId + imageUrl 반환
     */
    @PostMapping("/generate")
    public ApiResponse<ImageGenerateResultResponse> generate(
            @AuthenticationPrincipal String email,
            @RequestBody ImageGenerateRequest request
    ) {
        String userId = userIdExtractor.extractUserId(email);

        AiImageService.ImageResult result =
                aiImageService.generateImageFromSession(userId, request.sessionId());

        String imageUrl = "/ai/image/" + result.imageId();
        return ApiResponse.success(new ImageGenerateResultResponse(result.imageId(), imageUrl));
    }

    /**
     * (데모용: 인증 없이 공개) imageUrl로 접근하면 실제 이미지 바이너리를 내려준다.
     * <img src="/ai/image/{imageId}" /> 가능
     */
    @GetMapping(value = "/{imageId}", produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getImage(@PathVariable String imageId) {
        AiGeneratedImage img = aiImageService.getImagePublicOrThrow(imageId);
        return Base64.getDecoder().decode(img.getB64());
    }
}