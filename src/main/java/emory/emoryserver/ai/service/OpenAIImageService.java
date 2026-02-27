package emory.emoryserver.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAIImageService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.image.model:gpt-image-1}")
    private String model;

    @Value("${openai.image.n:1}")
    private int n;

    @Value("${openai.image.size:1024x1024}")
    private String size;

    @Value("${openai.image.output-format:png}")
    private String outputFormat;

    @Value("${openai.image.quality:auto}")
    private String quality;

    @Value("${openai.image.background:transparent}")
    private String background;

    public OpenAIImageService(
            @Value("${openai.image.base-url:${openai.base-url:https://api.openai.com}}") String baseUrl,
            @Value("${openai.webclient.max-in-memory-size:10485760}") int maxInMemorySize
    ) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies)
                .build();
    }

    /** 이미지 생성 결과(저장용) */
    public record GeneratedImage(String format, String b64) {}

    /**
     * OpenAI Images API (/v1/images/generations)
     * - DB 저장을 위해 "순수 base64"로 돌려준다.
     */
    public GeneratedImage generateImage(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is blank");
        }

        String fmt = (outputFormat == null || outputFormat.isBlank()) ? "png" : outputFormat;

        Map<String, Object> body = new HashMap<>();
        body.put("model", (model == null || model.isBlank()) ? "gpt-image-1" : model);
        body.put("prompt", prompt);
        body.put("n", Math.max(n, 1));
        body.put("size", (size == null || size.isBlank()) ? "1024x1024" : size);
        body.put("output_format", fmt);
        body.put("quality", (quality == null || quality.isBlank()) ? "auto" : quality);
        body.put("background", (background == null || background.isBlank()) ? "transparent" : background);

        OpenAIImagesResponse res = webClient.post()
                .uri("/v1/images/generations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r ->
                        r.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(err -> {
                                    log.error("[OpenAI Image] status={} body={}", r.statusCode(), err);
                                    return Mono.error(new RuntimeException("OpenAI image error: " + r.statusCode()));
                                })
                )
                .bodyToMono(OpenAIImagesResponse.class)
                .block();

        if (res == null || res.data == null || res.data.isEmpty()) {
            throw new RuntimeException("OpenAI image response is empty");
        }

        String b64 = res.data.get(0).b64_json;
        if (b64 == null || b64.isBlank()) {
            throw new RuntimeException("OpenAI image b64_json is empty");
        }

        return new GeneratedImage(fmt, b64);
    }

    static class OpenAIImagesResponse {
        public Long created;
        public List<ImageData> data;

        static class ImageData {
            public String b64_json;
            public String url;
        }
    }
}