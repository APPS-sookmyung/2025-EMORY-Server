package emory.emoryserver.ai.controller;

import emory.emoryserver.ai.dto.image.ImageRequestDto;
import emory.emoryserver.ai.dto.image.ImageResponseDto;
import emory.emoryserver.ai.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/image")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/generate")
    public ResponseEntity<ImageResponseDto> generate(@RequestBody ImageRequestDto req) {
        if (req == null || (req.getSessionId() == null && req.getDiaryContent() == null)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(imageService.generate(req));
    }
}
