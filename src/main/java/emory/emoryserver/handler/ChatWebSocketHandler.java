package emory.emoryserver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import emory.emoryserver.ai.dto.chat.ChatMessageRequestDto;
import emory.emoryserver.ai.dto.chat.ChatMessageResponseDto;
import emory.emoryserver.aidiary.model.AiDiary;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import emory.emoryserver.aidiary.service.AiDiaryService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final AiDiaryService aiDiaryService;
    private final Map<String, List<String>> chatLogs = new ConcurrentHashMap<>();
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 생성자 주입
    public ChatWebSocketHandler(AiDiaryService aiDiaryService) {
        this.aiDiaryService = aiDiaryService;
    }

    // 연결이 열릴 때 호출
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        chatLogs.put(session.getId(), new ArrayList<>());
        System.out.println("새 연결: " + session.getId());
    }

    // 메시지 수신 시 호출
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // JSON → DTO 변환
        ChatMessageRequestDto request = objectMapper.readValue(message.getPayload(), ChatMessageRequestDto.class);

        // 첫 메시지에서 userId 저장
        if (request.getUserId() != null && !sessionUserMap.containsKey(session.getId())) {
            sessionUserMap.put(session.getId(), request.getUserId());
        }
        // **종료 요청 처리**
        if ("FINISH_CHAT".equalsIgnoreCase(request.getType())) {
            List<String> logs = chatLogs.get(session.getId());
            String userId = sessionUserMap.get(session.getId());
            String sessionId = request.getSessionId();

            if (logs != null && userId != null) {
                AiDiary savedDiary = aiDiaryService.generateDiaryFromChat(logs, sessionId, userId);

                // diaryId 응답
                Map<String, Object> response = new HashMap<>();
                response.put("type", "DIARY_CREATED");
                response.put("diaryId", savedDiary.getId());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            }

            // 세션 정리 및 종료
            chatLogs.remove(session.getId());
            sessionUserMap.remove(session.getId());
            session.close();
            return;
        }


        // USER 메시지 로그 저장
        chatLogs.get(session.getId()).add("USER: " + request.getUserMessage());

        // Mock AI 응답 생성 (AI 연동 시 교체)
        ChatMessageResponseDto response = new ChatMessageResponseDto();
        response.setSessionId(request.getSessionId());
        response.setSender("AI");
        response.setMessage("AI 응답: " + request.getUserMessage());
        response.setTimestamp(LocalDateTime.now().toString());

        // AI 응답 로그 저장
        chatLogs.get(session.getId()).add("AI: " + response.getMessage());

        // 응답 전송
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    // 연결 종료 시 호출
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        List<String> logs = chatLogs.get(session.getId());
        String userId = sessionUserMap.get(session.getId());
        String sessionId = session.getId();

        try {
            // 일기 생성 서비스 호출 (MongoDB 저장)
            if (logs != null && userId != null) {
                AiDiary saveDiary = aiDiaryService.generateDiaryFromChat(logs, sessionId, userId);

                // 저장된 일기 id를 프론트로 전달 (일기 페이지 조회용)
                if (saveDiary != null && session.isOpen()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "DIARY_CREATED");
                    response.put("diaryId", saveDiary.getId());
                    session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(response)));
                }

            }
        } catch (Exception e) {
            System.err.println("일기 저장 중 오류 발생: " + e.getMessage());
        } finally {
            // 세션 데이터 정리
            chatLogs.remove(session.getId());
            sessionUserMap.remove(session.getId());
            System.out.println("연결 종료 및 일기 저장 완료: " + session.getId());
        }


    }

    // 에러 발생 시 응답
    private void sendErrorMessage(WebSocketSession session, String errorMsg) throws IOException {
        ChatMessageResponseDto errorResponse = new ChatMessageResponseDto();
        errorResponse.setSessionId(session.getId());
        errorResponse.setSender("System");
        errorResponse.setMessage("Error: " + errorMsg);
        errorResponse.setTimestamp(LocalDateTime.now().toString());

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
    }
}