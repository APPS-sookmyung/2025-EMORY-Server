package emory.emoryserver.aidiary.service;

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
public class OpenAIDiaryService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.diary.base-url}")
    private String baseUrl;

    @Value("${openai.diary.model}")
    private String diaryModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeneratedDiary generateDiary(String selectedEmotion, String calendarSummary, String transcript) {
        WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String instructions = """
                너는 사용자의 하루 대화를 바탕으로 '한국어 일기'를 작성하는 도우미다.
                반드시 JSON만 출력해라(코드블록 금지). 스키마:
                {"title": "...", "content": "...", "mood": "..."}
                - title: 20자 내외
                - content: 8~15문장, 자연스러운 1인칭 일기
                - mood: 한 단어(예: "기쁨", "불안", "평온", "우울", "분노", "설렘" 등)
                """;

        String input = """
                [선택 감정]
                %s

                [캘린더 요약]
                %s

                [대화 transcript]
                %s
                """.formatted(nullToDash(selectedEmotion), nullToDash(calendarSummary), transcript);

        Map<String, Object> body = Map.of(
                "model", diaryModel,
                "instructions", instructions,
                "input", input,
                "max_output_tokens", 1200,
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
            return new GeneratedDiary(
                    String.valueOf(json.getOrDefault("title", "오늘의 기록")),
                    String.valueOf(json.getOrDefault("content", "")),
                    String.valueOf(json.getOrDefault("mood", ""))
            );
        } catch (Exception e) {
            // 파싱 실패 시 fallback
            return new GeneratedDiary("오늘의 기록", outText, null);
        }
    }

    private String extractOutputText(Map resp) {
        if (resp == null) return "";
        Object direct = resp.get("output_text");
        if (direct != null) return String.valueOf(direct);

        Object outputObj = resp.get("output");
        if (!(outputObj instanceof List<?> output)) return "";

        // output 배열에서 message 텍스트를 최대한 안전하게 추출
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

    private String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    public record GeneratedDiary(String title, String content, String mood) {}
}
