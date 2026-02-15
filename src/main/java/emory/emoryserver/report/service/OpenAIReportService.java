package emory.emoryserver.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import emory.emoryserver.report.dto.EmotionStatDto;
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
public class OpenAIReportService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.report.base-url}")
    private String baseUrl;

    @Value("${openai.report.model}")
    private String reportModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @return 자연어 텍스트 (공감 + 제안)
     */
    public String generateFeedback(String dominantEmotion,
                                   List<EmotionStatDto> emotionStats,
                                   List<String> snippets) {

        WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String instructions = """
너는 사용자의 감정 리포트를 보고 공감과 실천 가능한 제안을 해주는 도우미다.

반드시 JSON만 출력해라(코드블록 금지). 스키마:
{"feedback":"..."}

규칙:
- feedback은 3~5문장, 너무 장황하지 않게.
- 1~2문장은 공감, 나머지는 현실적인 제안 2~3개(문장으로).
- 사용자가 실제로 말하지 않은 사실/사건은 만들지 마라.
""";

        String input = """
[주요 감정]
%s

[감정 분포(합 100)]
%s

[일기/대화 요약 스니펫(일부)]
%s
""".formatted(
                safe(dominantEmotion),
                safeJson(emotionStats),
                String.join("\n---\n", (snippets == null) ? List.of() : snippets)
        );

        Map<String, Object> body = Map.of(
                "model", reportModel,
                "instructions", instructions,
                "input", input,
                "max_output_tokens", 600,
                "truncation", "auto"
        );

        Map resp = wc.post()
                .uri("/v1/responses")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String outText = extractOutputText(resp);

        try {
            Map<String, Object> json = objectMapper.readValue(outText, Map.class);
            Object fb = json.get("feedback");
            return fb == null ? "" : String.valueOf(fb);
        } catch (Exception e) {
            // 파싱 실패 시 그냥 텍스트 반환(프론트에서라도 표시 가능)
            return outText == null ? "" : outText;
        }
    }

    private String extractOutputText(Map resp) {
        if (resp == null) return "";

        // responses API는 output_text를 바로 줄 수도 있음
        Object direct = resp.get("output_text");
        if (direct != null) return String.valueOf(direct);

        Object outputObj = resp.get("output");
        if (!(outputObj instanceof List<?> output)) return "";

        StringBuilder sb = new StringBuilder();
        for (Object item : output) {
            if (!(item instanceof Map<?, ?> m)) continue;
            if (!"message".equals(String.valueOf(m.get("type")))) continue;

            Object contentObj = m.get("content");
            if (!(contentObj instanceof List<?> parts)) continue;

            for (Object p : parts) {
                if (!(p instanceof Map<?, ?> pm)) continue;
                if ("output_text".equals(String.valueOf(pm.get("type"))) && pm.get("text") != null) {
                    sb.append(pm.get("text"));
                }
            }
        }
        return sb.toString().trim();
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String safeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }
}
