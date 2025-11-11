package emory.emoryserver.timecapsule.service;

import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.timecapsule.dto.ai.GeminiRequest;
import emory.emoryserver.timecapsule.dto.ai.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Service // <-- 이 클래스가 '구현체'임을 스프링에 알림
@Slf4j
public class GeminiTimecapsuleAiGenerator implements TimecapsuleAiGenerator {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;

    public GeminiTimecapsuleAiGenerator(WebClient.Builder webClientBuilder,
                                        @Value("${gemini.api.key}") String apiKey,
                                        @Value("${gemini.api.url}") String apiUrl) {
        this.webClient = webClientBuilder.build();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public String generateWeeklySummary(List<AiDiary> diaries, LocalDate weekStart, LocalDate weekEnd) {
        String prompt = createPrompt(diaries, weekStart, weekEnd);
        GeminiRequest requestBody = GeminiRequest.fromText(prompt);

        try {
            GeminiResponse response = webClient.post()
                    .uri(apiUrl, uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block(); // Service 레이어에서 동기적 결과를 기다림

            if (response != null) {
                String summary = response.getGeneratedText();
                if (summary != null) {
                    return summary;
                }
            }
            log.warn("Gemini API로부터 타임캡슐 요약 응답을 받지 못했습니다.");
            return "일기 요약을 생성하는 데 실패했습니다."; // 기본 실패 메시지

        } catch (Exception e) {
            log.error("Gemini API (타임캡슐) 호출 중 오류 발생: {}", e.getMessage(), e);
            return "AI 요약 서버에 오류가 발생했습니다."; // 예외 발생 시
        }
    }

    // Gemini AI에게 전달할 프롬프트

    private String createPrompt(List<AiDiary> diaries, LocalDate weekStart, LocalDate weekEnd) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("너는 다정하고 감성적인 일기 요약 전문가야.\n");
        promptBuilder.append(String.format("작년 %d월 %d일부터 %d일까지의 한 주간의 일기들이 주어질 거야.\n",
                weekStart.getMonthValue(), weekStart.getDayOfMonth(), weekEnd.getDayOfMonth()));
        promptBuilder.append("이 일기들을 바탕으로, 사용자가 추억을 회상할 수 있도록 따뜻한 톤으로 5문장의 요약문을 생성해 줘.\n");
        promptBuilder.append("일기에서 드러나는 주요 감정과 핵심 키워드를 자연스럽게 요약에 포함해 줘.\n\n");
        promptBuilder.append("--- 작년 주간 일기 목록 ---\n");

        for (AiDiary diary : diaries) {
            promptBuilder.append(String.format("\n[날짜: %s]\n", diary.getDateOfDay().toString()));
            promptBuilder.append(String.format("제목: %s\n", diary.getTitle()));
            promptBuilder.append(String.format("내용: %s\n", diary.getContent()));
            promptBuilder.append("---\n");
        }

        promptBuilder.append("\n위 일기들을 요약해 줘.");
        return promptBuilder.toString();
    }
}