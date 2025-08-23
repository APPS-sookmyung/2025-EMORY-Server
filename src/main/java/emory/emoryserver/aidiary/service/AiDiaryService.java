package emory.emoryserver.aidiary.service;

import emory.emoryserver.ai.model.ChatLog;
import emory.emoryserver.ai.repository.ChatMessageRepository;
import emory.emoryserver.aidiary.dto.DiaryGenerateRequestDto;
import emory.emoryserver.aidiary.dto.DiaryGenerateResponseDto;
import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.model.DiaryEdit;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiDiaryService {
    // private final ChatLogRepository ... // 세션/로그 조회 필요 시 주입
    private final AiDiaryRepository aiDiaryRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 1) DB 저장 대화로그 기반 생성 (실제 사용 버전)
     */
    public DiaryGenerateResponseDto generateDiaryFromSession(@Valid DiaryGenerateRequestDto req, String userId) {
        if (req.getSessionId() == null || req.getSessionId().isBlank()) {
            throw new IllegalArgumentException("sessionId는 필수입니다.");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }

        // (sessionId,userId) 기준으로 시간 오름차순 조회 (메서드명은 네 리포지토리에 맞춰 변경)
        List<ChatLog> messages = chatMessageRepository
                .findBysessionIdAndUserIdOrderByCreatedAtAsc(req.getSessionId(), userId);

        if (messages.isEmpty()) {
            throw new IllegalArgumentException("대화 로그가 없습니다. (sessionId=" + req.getSessionId() + ")");
        }

        // USER/AI 모두 포함해 content 구성
        String content = messages.stream()
                .map(m -> prefix(m.getRole()) + safe(m.getText()))
                .collect(Collectors.joining("\n"));

        // 너무 길면 컷
        if (content.length() > 10_000) content = content.substring(0, 10_000) + " …";

        // 제목: 첫 줄 20자
        String title = makeTitleFrom(content);

        // 일기 날짜: 요청값 or 마지막 메시지 시각 or 오늘
        LocalDate day = req.getDateOfDay() != null
                ? req.getDateOfDay()
                : (messages.get(messages.size() - 1).getCreatedAt() != null
                ? messages.get(messages.size() - 1).getCreatedAt().toLocalDate()
                : LocalDate.now());

        // DRAFT v1 저장 + history 스냅샷
        AiDiary diary = new AiDiary();
        diary.setSessionId(req.getSessionId());
        diary.setUserId(userId);
        diary.setTitle(title);                 // ✅ 누락되어 있던 부분 추가
        diary.setContent(content);
        diary.setMood(null);
        diary.setImageId(null);
        diary.setVersion(1);
        diary.setStatus("DRAFT");
        diary.setEditable(true);
        diary.setDateOfDay(day);
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());

        if (diary.getHistory() == null) diary.setHistory(new ArrayList<>());
        diary.getHistory().add(DiaryEdit.builder()
                .version(1)
                .title(title)
                .content(content)
                .mood(null)
                .editedAt(LocalDateTime.now())
                .editedBy("AI")
                .build());
        AiDiary saved = aiDiaryRepository.save(diary);
        return toResponse(saved);
    }

        // 구 입력값 join 버전 -테스트용으로만 유지
        public AiDiary generateDiaryFromChat (List < String > chatLogs, String sessionId, String userId){
            if (chatLogs == null || chatLogs.isEmpty()) {
                throw new IllegalArgumentException("chatLogs cannot be null or empty");
            }
            if (sessionId == null || sessionId.trim().isEmpty()) {
                throw new IllegalArgumentException("sessionId cannot be null or empty");
            }
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("userId cannot be null or empty");
            }
            String content = String.join("\n", chatLogs);
            String title = makeTitleFrom(content);

            AiDiary diary = new AiDiary();
            diary.setSessionId(sessionId);
            diary.setUserId(userId);
            diary.setTitle(title);             // ✅ 누락 보완
            diary.setContent(content);
            diary.setMood(null);
            diary.setImageId(null);
            diary.setVersion(1);
            diary.setStatus("DRAFT");
            diary.setEditable(true);
            diary.setCreatedAt(LocalDateTime.now());
            diary.setUpdatedAt(LocalDateTime.now());

            if (diary.getHistory() == null) diary.setHistory(new ArrayList<>());
            diary.getHistory().add(DiaryEdit.builder()
                    .version(1)
                    .title(title)
                    .content(content)
                    .mood(null)
                    .editedAt(LocalDateTime.now())
                    .editedBy("AI")
                    .build());

            return aiDiaryRepository.save(diary);
        }


        private String prefix(String role) {
            if (role == null) return "";
            String r = role.toLowerCase();
            if ("user".equals(r)) return "[USER] ";
            if ("assistant".equals(r)) return "[AI] ";
            return "";
        }

        private String safe(String s) {
            return (s == null) ? "" : s;
        }

        private String makeTitleFrom(String content) {
            if (content == null || content.isBlank()) return "오늘의 기록";
            String firstLine = content.strip().lines().findFirst().orElse("오늘의 기록");
            return firstLine.length() > 20 ? firstLine.substring(0, 20) + "…" : firstLine;
        }

        private DiaryGenerateResponseDto toResponse(final AiDiary d) {
            if (d == null) return null;
            return DiaryGenerateResponseDto.builder()
                    .diaryId(d.getId())
                    .title(d.getTitle())
                    .content(d.getContent())
                    .emotion(d.getMood())
                    .imageId(d.getImageId())
                    .version(d.getVersion())
                    .status(d.getStatus())
                    .editable(d.getEditable())
                    .date(d.getDateOfDay())
                    .createdAt(d.getCreatedAt())
                    .updatedAt(d.getUpdatedAt())
                    .build();
        }
    }
