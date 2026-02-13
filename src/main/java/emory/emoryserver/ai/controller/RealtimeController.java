package emory.emoryserver.ai.controller;

import emory.emoryserver.ai.dto.chat.RealtimeClientSecretResponseDto;
import emory.emoryserver.ai.service.ChatSessionService;
import emory.emoryserver.ai.service.OpenAIRealtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI Realtime", description = "OpenAI Realtime(WebRTC) 연결용 client secret 발급 API")
@RestController
@RequestMapping("/ai/realtime")
@RequiredArgsConstructor
public class RealtimeController {

    private final ChatSessionService chatSessionService;
    private final OpenAIRealtimeService openAIRealtimeService;

    @Operation(
            summary = "OpenAI Realtime clientSecret 발급",
            description = "세션 소유권을 검증한 뒤 OpenAI Realtime session을 생성하고 ephemeral key(client_secret)를 반환합니다."
    )
    @PostMapping("/client-secret")
    public RealtimeClientSecretResponseDto issueClientSecret(@RequestParam String sessionId) {
        // 1) 우리 세션 소유권 검증 (없으면 예외)
        chatSessionService.getOwnedSessionOrThrow(sessionId);

        // 2) OpenAI에 Realtime session 생성 요청 -> client_secret 받기
        Map<String, Object> resp = openAIRealtimeService.createRealtimeSession();

        // 3) client_secret.value / client_secret.expires_at 파싱
        Object csObj = resp.get("client_secret");
        if (!(csObj instanceof Map<?, ?> cs)) {
            throw new IllegalStateException("OpenAI 응답에 client_secret이 없습니다.");
        }

        Object value = cs.get("value");
        Object expiresAt = cs.get("expires_at");

        if (value == null || expiresAt == null) {
            throw new IllegalStateException("OpenAI client_secret.value/expires_at 누락");
        }

        Long expiresAtLong;
        if (expiresAt instanceof Number n) expiresAtLong = n.longValue();
        else expiresAtLong = Long.parseLong(String.valueOf(expiresAt));

        return RealtimeClientSecretResponseDto.builder()
                .clientSecret(String.valueOf(value))
                .expiresAt(expiresAtLong)
                .build();
    }
}
