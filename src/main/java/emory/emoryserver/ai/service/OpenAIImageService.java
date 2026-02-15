package emory.emoryserver.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${openai.image.model:gpt-4.1-mini}")
    private String imageModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @return base64 (png bytes) - prefix 없는 순수 base64 문자열
     */
    public String generateBase64Png(String prompt) {
        WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> body = Map.of(
                "model", imageModel,
                "input", prompt,
                "tools", List.of(Map.of("type", "image_generation")),
                "truncation", "auto"
        );

        Map resp = wc.post()
                .uri("/v1/responses")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return extractImageBase64(resp);
    }

    private String extractImageBase64(Map resp) {
        if (resp == null) return "";

        Object outputObj = resp.get("output");
        if (!(outputObj instanceof List<?> output)) return "";

        for (Object item : output) {
            if (!(item instanceof Map<?, ?> m)) continue;
            String type = String.valueOf(m.get("type"));
            if (!"image_generation_call".equals(type)) continue;

            Object result = m.get("result");
            // 공식 가이드 예시처럼 result가 base64 문자열로 오는 케이스
            if (result instanceof String s) return s;

            // 혹시 result가 map 구조로 오는 케이스도 방어
            if (result instanceof Map<?, ?> rm) {
                Object b64 = rm.get("b64_json");
                if (b64 != null) return String.valueOf(b64);
                Object image = rm.get("image_base64");
                if (image != null) return String.valueOf(image);
            }
        }
        return "";
    }
}
