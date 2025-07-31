package emory.emoryserver.ai.controller;

import emory.emoryserver.ai.dto.image.ImageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI image", description = "감정 기반 이미지 및 컬러 생성 API")
@RestController
@RequestMapping("/ai/image")
public class ImageController {

    @Operation(summary = "AI 이미지 및 컬러 생성", description = "감정 및 일기 내용을 기반으로 하루를 상징하는 이미지와 컬러를 생성합니다.")
    @PostMapping("/generate")
    public void generateImage(@RequestBody ImageRequestDto request) {
        // 이미지 및 컬러 생성 로직
    }
}
