package emory.emoryserver.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAIRealtimeService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.realtime.model}")
    private String realtimeModel;

    @Value("${openai.realtime.voice}")
    private String realtimeVoice;

    @Value("${openai.realtime.base-url}")
    private String baseUrl;

    // OpenAI Realtime Sessions: POST /v1/realtime/sessions
    // response.client_secret.value, response.client_secret.expires_at
    public Map<String, Object> createRealtimeSession() {
        WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> body = Map.of(
                "model", realtimeModel,
                "voice", realtimeVoice
        );

        return wc.post()
                .uri("/v1/realtime/sessions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
