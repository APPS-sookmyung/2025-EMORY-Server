package emory.emoryserver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import emory.emoryserver.ai.dto.chat.ChatMessageRequestDto;
import emory.emoryserver.ai.dto.chat.MessageType;
import emory.emoryserver.ai.model.ChatLog;
import emory.emoryserver.ai.repository.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatLogRepository chatLogRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 세션 오픈 시 별도 작업 없음
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // 1) payload → DTO
            ChatMessageRequestDto req =
                    objectMapper.readValue(message.getPayload(), ChatMessageRequestDto.class);

            // 2) 필수값 검증
            if (isBlank(req.getSessionId()) || isBlank(req.getUserId())) {
                sendErrorMessage(session, "INVALID_REQUEST", "sessionId/userId는 필수입니다.");
                return;
            }

            // 3) 타입 분기
            if (req.getType() == MessageType.MESSAGE) {
                if (isBlank(req.getUserMessage())) {
                    sendErrorMessage(session, "INVALID_REQUEST", "userMessage는 필수입니다.");
                    return;
                }

                // USER 한 줄 저장
                chatLogRepository.save(ChatLog.builder()
                        .sessionId(req.getSessionId())
                        .userId(req.getUserId())
                        .role("user")
                        .text(req.getUserMessage())
                        .createdAt(LocalDateTime.now())
                        .build());

                // (임시) AI 응답
                String aiReply = "많이 힘들었겠어요. 무엇이 가장 힘들었나요?";

                // AI 한 줄 저장
                chatLogRepository.save(ChatLog.builder()
                        .sessionId(req.getSessionId())
                        .userId(req.getUserId())
                        .role("assistant")
                        .text(aiReply)
                        .createdAt(LocalDateTime.now())
                        .build());

                // 클라이언트로 AI 메시지 전송
                safeSend(session, "AI_MESSAGE", new HashMap<>() {{
                    put("sessionId", req.getSessionId());
                    put("sender", "AI");
                    put("message", aiReply);
                    put("timestamp", LocalDateTime.now().toString());
                }});

            } else if (req.getType() == MessageType.FINISH_CHAT) {
                // 일기 생성은 REST API(/aidiary/diary/generate)에서 별도 수행
                safeSend(session, "FINISH_ACK", Map.of(
                        "message", "대화를 종료합니다. 필요 시 /aidiary/diary/generate API를 호출해 일기를 생성하세요.",
                        "sessionId", req.getSessionId()
                ));
                // 필요 시 세션 종료:
                session.close(CloseStatus.NORMAL);

            } else {
                sendErrorMessage(session, "INVALID_TYPE", "알 수 없는 type입니다: " + req.getType());
            }

        } catch (Exception e) {
            safeSend(session, "SERVER_ERROR", Map.of("message", "메시지 처리 중 오류가 발생했습니다."));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 정리 로직 필요 없으면 비움
    }

    /* helpers */

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void sendErrorMessage(WebSocketSession session, String code, String message) {
        safeSend(session, "ERROR", new HashMap<>() {{
            put("code", code);
            put("message", message);
        }});
    }

    private void safeSend(WebSocketSession session, String type, Map<String, Object> body) {
        try {
            Map<String, Object> payload = (body == null) ? new HashMap<>() : new HashMap<>(body);
            payload.put("type", type);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (Exception ignore) {}
    }
}
