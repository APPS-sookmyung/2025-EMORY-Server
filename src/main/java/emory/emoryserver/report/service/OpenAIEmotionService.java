package emory.emoryserver.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import emory.emoryserver.ai.model.ChatLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenAIEmotionService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    // report 설정 재사용
    @Value("${openai.report.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${openai.report.model:gpt-5}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 프론트 emotion-report.tsx 매핑과 맞추기 위해 다음 6개 카테고리만 반환:
     * HAPPY, SOSO, SAD, ANGRY, ANXIOUS, THOUGHTFUL
     *
     * @return Map<감정, 퍼센트(합100)>
     */
    public Map<String, Double> analyzeDistribution(List<ChatLog> logs) {
        String transcript = buildTranscriptForAnalysis(logs);

        // ✅ 디버그: 설정값 확인
        System.out.println("[EMORY][Emotion] baseUrl=" + baseUrl + ", model=" + model
                + ", apiKey=" + (openaiApiKey == null ? "null" : ("len=" + openaiApiKey.length())));

        WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // ✅ JSON만 강제
        String instructions = """
너는 사용자의 대화 transcript를 보고 '하루 감정 분포'를 추정한다.

반드시 아래 JSON 스키마를 '그대로' 출력해야 한다.
다른 글/설명/코드블록/마크다운을 절대 포함하지 마라.

{"distribution":{"HAPPY":0,"SOSO":0,"SAD":0,"ANGRY":0,"ANXIOUS":0,"THOUGHTFUL":0}}

규칙:
- 키는 정확히 6개만 사용: HAPPY, SOSO, SAD, ANGRY, ANXIOUS, THOUGHTFUL
- 값은 0~100 숫자(실수 가능)
- 합은 정확히 100
- 사용자가 말하지 않은 사건/사실을 만들지 마라.
""";

        String input = """
[대화 transcript]
%s
""".formatted(transcript);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("instructions", instructions);
        body.put("input", input);

        // ✅ 핵심: reasoning을 줄여서 텍스트(JSON) 출력이 나오게 함
        body.put("reasoning", Map.of("effort", "minimal"));

        body.put("max_output_tokens", 800);
        body.put("truncation", "auto");

        // ✅ gpt-5에서 temperature 미지원 -> 절대 넣지 말기 (지금 400 원인)
        // body.put("temperature", 0.2);

        // ✅ JSON 안정성: text.format을 json_schema로 강제 (Responses API 지원)  :contentReference[oaicite:1]{index=1}
        body.put("text", Map.of(
                "format", Map.of(
                        "type", "json_schema",
                        "name", "emotion_distribution",
                        "description", "A distribution of daily emotions that sums to 100.",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "required", List.of("distribution"),
                                "properties", Map.of(
                                        "distribution", Map.of(
                                                "type", "object",
                                                "additionalProperties", false,
                                                "required", List.of("HAPPY", "SOSO", "SAD", "ANGRY", "ANXIOUS", "THOUGHTFUL"),
                                                "properties", Map.of(
                                                        "HAPPY", Map.of("type", "number", "minimum", 0, "maximum", 100),
                                                        "SOSO", Map.of("type", "number", "minimum", 0, "maximum", 100),
                                                        "SAD", Map.of("type", "number", "minimum", 0, "maximum", 100),
                                                        "ANGRY", Map.of("type", "number", "minimum", 0, "maximum", 100),
                                                        "ANXIOUS", Map.of("type", "number", "minimum", 0, "maximum", 100),
                                                        "THOUGHTFUL", Map.of("type", "number", "minimum", 0, "maximum", 100)
                                                )
                                        )
                                )
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

            // ✅ 여기! 네가 붙여넣은 catch는 "이 try 블록 바로 아래"에 들어가면 됨
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            System.out.println("[EMORY][Emotion] OpenAI request FAILED: " + e.getRawStatusCode());
            System.out.println("[EMORY][Emotion] errorBody=" + e.getResponseBodyAsString());
            e.printStackTrace();
            return defaultDist();
        } catch (Exception e) {
            System.out.println("[EMORY][Emotion] OpenAI request FAILED");
            e.printStackTrace();
            return defaultDist();
        }

        // ✅ 디버그: 원문 응답 확인
        System.out.println("[EMORY][Emotion] rawResp=" + safeToString(resp));

        // ✅ status가 incomplete면 fallback (중요)
        String status = (resp == null) ? null : String.valueOf(resp.get("status"));
        if (status != null && !"completed".equalsIgnoreCase(status)) {
            System.out.println("[EMORY][Emotion] status not completed => fallback. status=" + status);
            return defaultDist();
        }

        String outText = extractOutputText(resp);
        System.out.println("[EMORY][Emotion] outText=" + outText);

        // ✅ outText가 빈 경우 rawResp 상태도 찍고 fallback (중요)
        if (outText == null || outText.trim().isEmpty()) {
            System.out.println("[EMORY][Emotion] outText empty => fallback. status=" + status);
            System.out.println("[EMORY][Emotion] rawResp(again)=" + safeToString(resp));
            return defaultDist();
        }

        Map<String, Double> dist = parseDistribution(outText);
        return normalizeTo100(dist);
    }

    private String buildTranscriptForAnalysis(List<ChatLog> logs) {
        if (logs == null || logs.isEmpty()) return "-";

        StringBuilder sb = new StringBuilder();
        int maxChars = 5000;

        for (ChatLog log : logs) {
            if (log == null) continue;
            String role = log.getRole() == null ? "" : log.getRole().trim().toLowerCase();
            String text = log.getText() == null ? "" : log.getText().trim();
            if (text.isBlank()) continue;

            if ("user".equals(role)) {
                sb.append("USER: ").append(text).append("\n");
            } else if ("assistant".equals(role)) {
                String cut = text.length() > 120 ? text.substring(0, 120) + "…" : text;
                sb.append("ASSISTANT: ").append(cut).append("\n");
            }

            if (sb.length() >= maxChars) break;
        }

        String result = sb.toString().trim();
        return result.isBlank() ? "-" : result;
    }

    private Map<String, Double> parseDistribution(String outText) {
        try {
            if (outText == null) return defaultDist();
            String trimmed = outText.trim();
            if (trimmed.isEmpty()) return defaultDist();

            // ✅ 혹시 JSON 앞뒤에 잡텍스트 붙으면 { ... }만 잘라내기
            String json = trimToJsonObject(trimmed);

            Map<String, Object> root = objectMapper.readValue(json, Map.class);
            Object d = root.get("distribution");
            if (!(d instanceof Map<?, ?> m)) {
                System.out.println("[EMORY][Emotion] distribution missing in JSON");
                return defaultDist();
            }

            Map<String, Double> dist = new HashMap<>();
            dist.put("HAPPY", toDouble(m.get("HAPPY")));
            dist.put("SOSO", toDouble(m.get("SOSO")));
            dist.put("SAD", toDouble(m.get("SAD")));
            dist.put("ANGRY", toDouble(m.get("ANGRY")));
            dist.put("ANXIOUS", toDouble(m.get("ANXIOUS")));
            dist.put("THOUGHTFUL", toDouble(m.get("THOUGHTFUL")));

            // ✅ 누락 키 방어
            for (String k : List.of("HAPPY", "SOSO", "SAD", "ANGRY", "ANXIOUS", "THOUGHTFUL")) {
                dist.putIfAbsent(k, 0.0);
            }

            return dist;
        } catch (Exception e) {
            System.out.println("[EMORY][Emotion] parseDistribution FAILED");
            e.printStackTrace();
            return defaultDist();
        }
    }

    private String trimToJsonObject(String s) {
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return s.substring(start, end + 1);
        }
        return s;
    }

    private Map<String, Double> defaultDist() {
        Map<String, Double> m = new LinkedHashMap<>();
        m.put("HAPPY", 16.67);
        m.put("SOSO", 16.67);
        m.put("SAD", 16.67);
        m.put("ANGRY", 16.67);
        m.put("ANXIOUS", 16.66);
        m.put("THOUGHTFUL", 16.66);
        return m;
    }

    private double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Map<String, Double> normalizeTo100(Map<String, Double> dist) {
        if (dist == null || dist.isEmpty()) return defaultDist();

        for (String k : dist.keySet()) {
            Double v = dist.get(k);
            if (v == null || v < 0) dist.put(k, 0.0);
        }

        double sum = dist.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum <= 0.000001) return defaultDist();

        Map<String, Double> norm = new LinkedHashMap<>();
        double acc = 0.0;
        List<String> keys = List.of("HAPPY", "SOSO", "SAD", "ANGRY", "ANXIOUS", "THOUGHTFUL");
        for (String k : keys) {
            double raw = dist.getOrDefault(k, 0.0);
            double v = (raw / sum) * 100.0;
            v = Math.round(v * 100.0) / 100.0;
            norm.put(k, v);
            acc += v;
        }

        double diff = Math.round((100.0 - acc) * 100.0) / 100.0;
        String lastKey = "THOUGHTFUL";
        norm.put(lastKey, Math.round((norm.get(lastKey) + diff) * 100.0) / 100.0);

        return norm;
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

    private String safeToString(Object o) {
        try {
            return String.valueOf(o);
        } catch (Exception e) {
            return "<toString failed>";
        }
    }
}
