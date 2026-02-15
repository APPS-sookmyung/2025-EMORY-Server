package emory.emoryserver.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import emory.emoryserver.report.dto.EmotionStatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAIReportService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    // emotion 서비스랑 동일하게 기본값 두는 걸 추천(없으면 자동으로 api.openai.com)
    @Value("${openai.report.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${openai.report.model:gpt-5}")
    private String reportModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @return 자연어 텍스트 (공감 + 제안)
     */
    public String generateFeedback(String dominantEmotion,
                                   List<EmotionStatDto> emotionStats,
                                   List<String> snippets) {

        // ✅ 디버그: 설정값 확인
        System.out.println("[EMORY][Report] baseUrl=" + baseUrl + ", model=" + reportModel
                + ", apiKey=" + (openaiApiKey == null ? "null" : ("len=" + openaiApiKey.length())));

        WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String instructions = """
너는 사용자의 감정 리포트를 보고 공감과 실천 가능한 제안을 해주는 도우미다.

반드시 JSON만 출력해라(코드블록/마크다운/설명 금지). 스키마:
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

        // ✅ Responses API body 구성 (json_schema로 JSON 강제)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", reportModel);
        body.put("instructions", instructions);
        body.put("input", input);
        body.put("reasoning", Map.of("effort", "minimal"));
        body.put("max_output_tokens", 600);
        body.put("truncation", "auto");

        // ✅ JSON 스키마 강제 (프롬프트에 이상한 텍스트 끼어드는 것 방지)
        body.put("text", Map.of(
                "format", Map.of(
                        "type", "json_schema",
                        "name", "daily_feedback",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "feedback", Map.of("type", "string")
                                ),
                                "required", List.of("feedback")
                        )
                )
        ));

        Map resp;
        try {
            resp = wc.post()
                    .uri("/v1/responses")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            System.out.println("[EMORY][Report] OpenAI feedback request FAILED: " + e.getRawStatusCode());
            System.out.println("[EMORY][Report] errorBody=" + e.getResponseBodyAsString());
            e.printStackTrace();
            return defaultFeedback(dominantEmotion);
        } catch (Exception e) {
            System.out.println("[EMORY][Report] OpenAI feedback request FAILED");
            e.printStackTrace();
            return defaultFeedback(dominantEmotion);
        }

        // ✅ 디버그: 원문 응답 확인
        System.out.println("[EMORY][Report] rawResp=" + safeToString(resp));

        String outText = extractOutputText(resp);
        System.out.println("[EMORY][Report] outText=" + outText);

        // ✅ 핵심 방어: outText 비면 status 찍고 fallback
        if (outText == null || outText.trim().isEmpty()) {
            Object status = (resp == null ? null : resp.get("status"));
            System.out.println("[EMORY][Report] feedback outText empty -> fallback. status=" + status);
            return defaultFeedback(dominantEmotion);
        }

        try {
            // outText는 {"feedback":"..."} 형태일 것으로 기대
            String jsonOnly = trimToJsonObject(outText.trim());
            Map<String, Object> json = objectMapper.readValue(jsonOnly, Map.class);
            Object fb = json.get("feedback");
            String fbText = (fb == null) ? "" : String.valueOf(fb).trim();
            return fbText.isEmpty() ? defaultFeedback(dominantEmotion) : fbText;
        } catch (Exception e) {
            // 파싱 실패 시 fallback (프론트에 null 안 뜨게)
            System.out.println("[EMORY][Report] parse feedback FAILED -> fallback");
            e.printStackTrace();
            return defaultFeedback(dominantEmotion);
        }
    }

    /**
     * ✅ Responses API 응답에서 텍스트를 최대한 뽑아오기(유연)
     */
    private String extractOutputText(Map resp) {
        if (resp == null) return "";

        Object direct = resp.get("output_text");
        if (direct != null) {
            String s = String.valueOf(direct).trim();
            if (!s.isEmpty()) return s;
        }

        Object outputObj = resp.get("output");
        if (!(outputObj instanceof List<?> output)) return "";

        StringBuilder sb = new StringBuilder();
        for (Object item : output) {
            if (!(item instanceof Map<?, ?> m)) continue;

            Object contentObj = m.get("content");
            if (!(contentObj instanceof List<?> parts)) continue;

            for (Object p : parts) {
                if (!(p instanceof Map<?, ?> pm)) continue;

                Object text = pm.get("text");
                if (text != null) sb.append(text);

                Object value = pm.get("value");
                if (value != null) sb.append(value);
            }
        }
        return sb.toString().trim();
    }

    private String trimToJsonObject(String s) {
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
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

    private String defaultFeedback(String dominantEmotion) {
        String emo = (dominantEmotion == null || dominantEmotion.isBlank()) ? "오늘" : dominantEmotion;
        return "오늘은 " + emo + " 감정이 특히 두드러진 하루였네요. "
                + "그만큼 마음이 많이 바빴을 수 있어요. "
                + "지금 할 수 있는 작은 것부터 해보면 좋아요: 물 한 잔 마시기, 5분만 호흡 고르기, "
                + "내일 꼭 해야 할 일 1개만 적어두기.";
    }

    private String safeToString(Object o) {
        try {
            return String.valueOf(o);
        } catch (Exception e) {
            return "<toString failed>";
        }
    }
}
