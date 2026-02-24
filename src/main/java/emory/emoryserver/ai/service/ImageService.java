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
        String image = openAIImageService.generateImage(prompt);

        return new ImageGenerateResponse(
                image,
                props.image().outputFormat(),
                props.image().model(),
                props.image().size()
        );
    }
}
