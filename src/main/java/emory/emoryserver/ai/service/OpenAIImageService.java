package emory.emoryserver.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAIImageService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.image.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${openai.image.model:gpt-image-1}")
    private String imageModel;

    @Value("${openai.image.size:1024x1024}")
    private String imageSize;

    /**
     * @return base64 png 문자열 (raw b64, "data:image/png;base64," prefix 없음)
     */
    public String generateBase64Png(String prompt) {
        WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> body = Map.of(
                "model", imageModel,
                "prompt", prompt,
                "size", imageSize,
                "n", 1,
                "response_format", "b64_json"
        );

        Map resp = wc.post()
                .uri("/v1/images/generations")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // resp.data[0].b64_json
        Object dataObj = (resp == null) ? null : resp.get("data");
        if (!(dataObj instanceof List<?> data) || data.isEmpty()) return "";

        Object first = data.get(0);
        if (!(first instanceof Map<?, ?> m)) return "";

        Object b64 = m.get("b64_json");
        return b64 == null ? "" : String.valueOf(b64);
    }
}

