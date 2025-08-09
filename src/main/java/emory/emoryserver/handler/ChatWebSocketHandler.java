package emory.emoryserver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import emory.emoryserver.ai.dto.chat.ChatMessageRequestDto;
import emory.emoryserver.ai.dto.chat.MessageType;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import emory.emoryserver.aidiary.service.AiDiaryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 채팅 핸들러
 * - MESSAGE: 일반 사용자 메시지 → 로그 저장, Mock AI 응답
 * - FINISH_CHAT: 대화 종료 → 일기 생성 시도 후 종료 ACK
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final AiDiaryService aiDiaryService;
    private final ObjectMapper objectMapper;

    /**
     * 세션ID -> 채팅 로그 (간단히 String 합으로 저장: "USER: ..." / "AI: ...")
     */
    private final Map<String, List<String>> chatLogs = new ConcurrentHashMap<>();
    /**
     * 세션ID -> userId
     */
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();


    // 생성자 주입
    public ChatWebSocketHandler(ObjectMapper objectMapper, AiDiaryService aiDiaryService) {
        this.objectMapper = objectMapper;
        this.aiDiaryService = aiDiaryService;
    }

    // 연결이 열릴 때 호출
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        chatLogs.put(session.getId(), new ArrayList<>());
    }

    // 메시지 수신 시 호출
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            //Json -> Dto
            ChatMessageRequestDto req =
                    objectMapper.readValue(message.getPayload(), ChatMessageRequestDto.class);

            // 공통 필수값 검증
            if (isBlank(req.getSessionId()) || isBlank(req.getUserId())) {
                sendErrorMessage(session, "INVALID_REQUEST", "sessionId/userId는 필수입니다.");
                return;
            }
            // 세션-사용자 매핑
            sessionUserMap.put(session.getId(), req.getUserId());

            // type 분기
            if (req.getType() == MessageType.MESSAGE) {
                if (isBlank(req.getUserMessage())) {
                    sendErrorMessage(session, "INVALID_REQUEST", "userMessage는 필수입니다.");
                    return;
                }

                // USER 메시지 로그 저장
                chatLogs.computeIfAbsent(session.getId(), k -> new ArrayList<>())
                        .add("[" + LocalDateTime.now() + "] USER: " + req.getUserMessage());
                // AI 응답 생성/전송
                String aiReply = "많이 힘들었겠어요. 무엇이 가장 힘들었나요?";
                chatLogs.get(session.getId())
                        .add("[" + LocalDateTime.now() + "] AI: " + aiReply);
                // 클라이언트 전송
                safeSend(session, "AI_MESSAGE", Map.of(
                        "sessionId", req.getSessionId(),
                        "sender", "AI",
                        "message", aiReply,
                        "timestamp", LocalDateTime.now().toString()
                ));

            } else if (req.getType() == MessageType.FINISH_CHAT) {
                // 일기 생성 시도 (서비스 시그니처 맞춰서 호출)
                try {
                    List<String> logs = chatLogs.get(session.getId());
                    String userId = sessionUserMap.get(session.getId());
                    if (logs != null && userId != null) {
                        //섭스 시그니처가 (logs, sessionId, userId)라면 이렇게:
                        aiDiaryService.generateDiaryFromChat(logs, req.getSessionId(), userId);
                    }
                    safeSend(session, "FINISH_ACK", Map.of(
                            "message", "대화를 종료합니다.",
                            "sessionId", req.getSessionId()
                    ));
                } catch (Exception e) {
                    System.err.println("[WS][FINISH_CHAT] diary gen fail: " + e.getMessage());
                    sendErrorMessage(session, "DIARY_GENERATION_FAILED", "일기 생성에 실패했습니다.");
                }
                // 종료는 선택 사항: 클라이언트 정책에 맞춰 결정
                // session.close(CloseStatus.NORMAL);

            } else {
                sendErrorMessage(session, "INVALID_TYPE", "알 수 없는 type입니다: " + req.getType());
            }

        } catch (Exception e) {
            System.err.println("[WS][handleTextMessage] " + e.getMessage());
            safeSend(session, "SERVER_ERROR", Map.of("message", "메시지 처리 중 오류가 발생했습니다."));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            // 세션 종료 시 정리 (여기서 추가로 일기 저장을 트리거하고 싶다면 try/catch로 감싸서 호출)
            // 이미 FINISH_CHAT에서 처리한다면 생략 가능
        } catch (Exception e) {
            System.err.println("[WS][afterConnectionClosed] " + e.getMessage());
        } finally {
            sessionUserMap.remove(session.getId());
            chatLogs.remove(session.getId());
        }
    }

    /* ------------ Helper methods ------------ */

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void sendErrorMessage(WebSocketSession session, String code, String message) {
        safeSend(session, "ERROR", new java.util.HashMap<String, Object>() {{
            put("code", code);
            put("message", message);
        }});
    }

    /**
     * 클라이언트로 안전하게 메시지 전송
     *
     * @param session 웹소켓 세션
     * @param type    메시지 타입 (예: ERROR, AI_MESSAGE, FINISH_ACK)
     * @param body    추가 데이터 (없으면 null 가능)
     */
    private void safeSend(WebSocketSession session, String type, Map<String, Object> body) {
        try {
            Map<String, Object> payload = (body == null) ? new java.util.HashMap<>() : new java.util.HashMap<>(body);
            payload.put("type", type);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        } catch (Exception ignore) {}
    }
}