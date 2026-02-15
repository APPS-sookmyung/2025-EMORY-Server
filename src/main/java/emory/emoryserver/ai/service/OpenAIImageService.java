package emory.emoryserver.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
public class OpenAIImageService {

    private final WebClient wc;
    private final String imageModel; // ex) gpt-image-1

    public OpenAIImageService(
            WebClient.Builder builder,
            @org.springframework.beans.factory.annotation.Value("${openai.api.key}") String apiKey,
            @org.springframework.beans.factory.annotation.Value("${openai.report.base-url}") String baseUrl,
            @org.springframework.beans.factory.annotation.Value("${openai.image.model:gpt-image-1}") String imageModel
    ) {
        this.imageModel = imageModel;
        this.wc = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String generateBase64Png(String prompt) {
        try {
            Map<String, Object> body = Map.of(
                    "model", imageModel,
                    "prompt", prompt,
                    "size", "1024x1024",
                    "response_format", "b64_json"
            );

            Map resp = wc.post()
                    .uri("/v1/images/generations")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            r -> r.bodyToMono(String.class).flatMap(errBody -> {
                                log.error("[OpenAI Image] status={} body={}", r.statusCode(), errBody);
                                return Mono.error(new RuntimeException("OpenAI image error: " + r.statusCode()));
                            })
                    )
                    .bodyToMono(Map.class)
                    .block();

            // TODO: resp에서 b64_json 파싱해서 return
            // resp 구조: { "data": [ { "b64_json": "...." } ] }
            var data = (java.util.List<Map<String, Object>>) resp.get("data");
            if (data == null || data.isEmpty()) return "";
            Object b64 = data.get(0).get("b64_json");
            return b64 == null ? "" : String.valueOf(b64);

        } catch (WebClientResponseException e) {
            // 혹시 onStatus 못 타는 케이스 대비
            log.error("[OpenAI Image] HTTP {} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        }
    }
}
