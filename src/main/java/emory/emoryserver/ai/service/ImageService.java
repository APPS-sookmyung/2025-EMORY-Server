package emory.emoryserver.ai.service;

import emory.emoryserver.ai.config.OpenAIProperties;
import emory.emoryserver.ai.dto.ImageGenerateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final OpenAIImageService openAIImageService;
    private final OpenAIProperties props;

    public ImageGenerateResponse generate(String prompt) {
        OpenAIImageService.GeneratedImage gen = openAIImageService.generateImage(prompt);

        // 기존 ImageGenerateResponse가 "base64 또는 data_uri" 를 받는 구조라서
        // 프론트 호환을 위해 data_uri 형태로 돌려줌
        String imageDataUri = "data:image/" + gen.format() + ";base64," + gen.b64();

        return new ImageGenerateResponse(
                imageDataUri,
                gen.format(),                 // props.image().outputFormat() 대신 실제 생성 결과 사용
                props.image().model(),
                props.image().size()
        );
    }
}
