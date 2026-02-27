package emory.emoryserver.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class OpenAIImagePromptService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.image.prompt-model:gpt-5}")
    private String promptModel;

    public OpenAIImagePromptService(
            @Value("${openai.base-url:https://api.openai.com}") String baseUrl,
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

    /**
     * 대화 로그(text) → 이미지 생성용 프롬프트(한 문단)로 변환
     */
    public String buildImagePrompt(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            throw new IllegalArgumentException("transcript is blank");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("openai.api.key is missing");
        }

        Map<String, Object> body = Map.of(
                "model", promptModel,
                "input", "아래 대화 내용을 바탕으로, 하루를 상징하는 이미지를 생성하려고 해.\n" +
                        "요구사항:\n" +
                        "- 출력은 '이미지 생성용 프롬프트'만 1개, 한국어 1문단으로.\n" +
                        "- 인물의 구체적 신상/실명/민감정보는 절대 포함하지 마.\n" +
                        "- 장면(장소/시간대/조명), 분위기(감정), 핵심 오브젝트 2~4개, 색감/스타일(예: 일러스트/사진/수채화 등) 포함.\n" +
                        "- 너무 길지 않게(대략 2~5문장).\n" +
                        "- 대화에 없는 사실을 지어내지 마.\n\n" +
                        "[대화]\n" + transcript
        );

        try {
            // 1) 응답을 raw로 받아서 구조 변화/에러를 그대로 확인 가능하게
            String raw = webClient.post()
                    .uri("/v1/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[OpenAI Image Prompt] raw={}", raw);

            if (raw == null || raw.isBlank()) {
                throw new RuntimeException("OpenAI response is empty");
            }

            JsonNode root = objectMapper.readTree(raw);

            // 2) 에러 응답이면 메시지를 그대로 보여주기 (원인 파악 최우선)
            JsonNode err = root.get("error");
            if (err != null && !err.isNull()) {
                String msg = err.path("message").asText("unknown error");
                String type = err.path("type").asText("");
                String code = err.path("code").asText("");
                throw new RuntimeException("OpenAI error: " + msg
                        + (type.isBlank() ? "" : " (type=" + type + ")")
                        + (code.isBlank() ? "" : " (code=" + code + ")"));
            }

            // 3) 혹시 helper 필드 output_text 가 있으면 그걸 우선 사용
            String outputText = root.path("output_text").asText("");
            if (!outputText.isBlank()) {
                return outputText.trim();
            }

            // 4) 일반적인 Responses 구조에서 text 추출: output[].content[].text
            JsonNode output = root.path("output");
            if (output.isArray()) {
                for (JsonNode item : output) {
                    JsonNode content = item.path("content");
                    if (!content.isArray()) continue;

                    for (JsonNode c : content) {
                        String t = c.path("text").asText("");
                        if (!t.isBlank()) return t.trim();
                    }
                }
            }

            throw new RuntimeException("OpenAI prompt response has no text. raw=" + raw);

        } catch (Exception e) {
            log.error("[OpenAI Image Prompt] failed", e);
            throw new RuntimeException("Failed to build image prompt");
        }
    }
}