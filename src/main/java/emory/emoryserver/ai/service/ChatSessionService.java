package emory.emoryserver.ai.service;

import emory.emoryserver.ai.dto.chat.ChatSaveRequestDto;
import emory.emoryserver.ai.dto.chat.ChatTurnDto;
import emory.emoryserver.ai.model.ChatLog;
import emory.emoryserver.ai.model.ChatSession;
import emory.emoryserver.ai.repository.ChatLogRepository;
import emory.emoryserver.ai.repository.ChatSessionRepository;
import emory.emoryserver.report.service.DailyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatLogRepository chatLogRepository;
    private final DailyReportService dailyReportService;

    public String startSession(String selectedEmotion, String calendarSummary) {
        String userId = currentUserId();

        String sessionId = UUID.randomUUID().toString();
        ChatSession session = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .selectedEmotion(selectedEmotion)
                .calendarSummary(calendarSummary)
                .status("ACTIVE")
                .startedAt(LocalDateTime.now())
                .build();

        chatSessionRepository.save(session);
        return sessionId;
    }

    public void stopSession(String sessionId) {
        ChatSession session = getOwnedSessionOrThrow(sessionId);
        session.setStatus("STOPPED");
        session.setEndedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
    }

    public void saveConversation(ChatSaveRequestDto request) {
        ChatSession session = getOwnedSessionOrThrow(request.getSessionId());

        List<ChatTurnDto> turns = (request.getMessages() == null) ? List.of() : request.getMessages();
        List<ChatLog> logs = new ArrayList<>();

        for (ChatTurnDto t : turns) {
            if (t == null) continue;
            String role = (t.getRole() == null) ? "" : t.getRole().trim();
            String text = (t.getText() == null) ? "" : t.getText().trim();
            if (role.isEmpty() || text.isEmpty()) continue;

            logs.add(ChatLog.builder()
                    .sessionId(session.getId())
                    .userId(session.getUserId())
                    .role(role)           // "user" | "assistant"
                    .text(text)
                    .createdAt(LocalDateTime.now()) // timestamp 있으면 파싱해서 넣는 것도 가능
                    .build());
        }

        if (!logs.isEmpty()) {
            chatLogRepository.saveAll(logs);
        }

        session.setStatus("SAVED");
        session.setSavedAt(LocalDateTime.now());
        if (session.getEndedAt() == null) session.setEndedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        // TODO: 여기서 후처리 트리거 (감정분석/일기생성) 붙이면 됨
        // ex) aidiary generate 호출용 이벤트 발행/큐 enqueue 등
        try {
            LocalDate date = session.getEndedAt().toLocalDate();
            List<ChatLog> fullLogs = chatLogRepository.findBySessionIdAndUserIdOrderByCreatedAtAsc(
                    session.getId(),
                    session.getUserId()
            );

            dailyReportService.createOrUpdateDailyReportFromSession(session.getUserId(), date, fullLogs);
        } catch (Exception e) {
            // 리포트 생성 실패해도 대화 저장은 성공해야 함
        }
    }

    public ChatSession getOwnedSessionOrThrow(String sessionId) {
        String userId = currentUserId();
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션이 존재하지 않습니다. sessionId=" + sessionId));

        if (!userId.equals(session.getUserId())) {
            throw new SecurityException("세션 접근 권한이 없습니다.");
        }
        return session;
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new SecurityException("인증 정보가 없습니다.");
        }
        return auth.getName(); // JWT subject(email)
    }
}
