package emory.emoryserver.aidiary.service;

import emory.emoryserver.ai.model.ChatLog;
import emory.emoryserver.ai.repository.ChatLogRepository;
import emory.emoryserver.aidiary.dto.DiaryGenerateRequestDto;
import emory.emoryserver.aidiary.dto.DiaryGenerateResponseDto;
import emory.emoryserver.aidiary.dto.DiarySaveRequestDto;
import emory.emoryserver.aidiary.dto.DiaryUpdateRequestDto;
import emory.emoryserver.aidiary.exception.DiaryNotFoundException;
import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.model.DiaryEdit;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ConditionalOnProperty(name="features.diary.enabled", havingValue="true", matchIfMissing=false)
@Service
@RequiredArgsConstructor
public class AiDiaryService {

    private final AiDiaryRepository aiDiaryRepository;
    private final ChatLogRepository chatLogRepository;
    private final OpenAIDiaryService openAIDiaryService;

    /**
     * 1) DB 저장 대화로그 기반 생성 (실제 사용 버전)
     * - sessionId로 대화로그 조회
     * - transcript 구성
     * - OpenAI로 일기 생성(title/content/mood)
     * - DRAFT v1 저장 + history 적재
     */
    public DiaryGenerateResponseDto generateDiaryFromSession(@Valid DiaryGenerateRequestDto req, String userId) {
        if (req.getSessionId() == null || req.getSessionId().isBlank()) {
            throw new IllegalArgumentException("sessionId는 필수입니다.");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }

        // (sessionId,userId) 기준으로 시간 오름차순 조회
        List<ChatLog> logs = chatLogRepository
                .findBySessionIdAndUserIdOrderByCreatedAtAsc(req.getSessionId(), userId);

        if (logs.isEmpty()) {
            throw new IllegalArgumentException("대화 로그가 없습니다. (sessionId=" + req.getSessionId() + ", userId=" + userId + ")");
        }

        // USER/AI 모두 포함해 transcript 구성
        String transcript = logs.stream()
                .map(m -> prefix(m.getRole()) + safe(m.getText()))
                .collect(Collectors.joining("\n"));

        // 너무 길면 컷 (일단 간단 컷. 추후 chunk/요약로직으로 개선 가능)
        if (transcript.length() > 10_000) transcript = transcript.substring(0, 10_000) + " …";

        // 일기 날짜: 요청값 or 마지막 메시지 시각 or 오늘
        LocalDate day = req.getDateOfDay() != null
                ? req.getDateOfDay()
                : (logs.get(logs.size() - 1).getCreatedAt() != null
                ? logs.get(logs.size() - 1).getCreatedAt().toLocalDate()
                : LocalDate.now());

        // ✅ LLM 호출 (선택감정/캘린더요약은 지금 req에 없으면 null로)
        // - 추후 ChatSession에서 selectedEmotion/calendarSummary 가져오려면 여기 인자만 채우면 됨
        OpenAIDiaryService.GeneratedDiary gen = openAIDiaryService.generateDiary(
                null,            // selectedEmotion (추후 연결)
                null,            // calendarSummary (추후 연결)
                transcript       // transcript
        );

        String title = (gen != null && gen.title() != null && !gen.title().isBlank())
                ? gen.title()
                : makeTitleFrom(transcript);

        String diaryContent = (gen != null && gen.content() != null)
                ? gen.content()
                : transcript;

        String mood = (gen != null && gen.mood() != null && !gen.mood().isBlank())
                ? gen.mood()
                : null;

        // DRAFT v1 저장 + history 스냅샷
        AiDiary d = new AiDiary();
        d.setSessionId(req.getSessionId());
        d.setUserId(userId);

        d.setTitle(title);
        d.setContent(diaryContent);
        d.setMood(mood);

        d.setImageId(null);
        d.setScraped(false);

        d.setVersion(1);
        d.setStatus("DRAFT");
        d.setEditable(true);

        d.setDateOfDay(day);
        d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());

        // ✅ history는 v1을 1번만 넣기
        d.setHistory(new ArrayList<>());
        d.getHistory().add(DiaryEdit.builder()
                .version(1)
                .title(title)
                .content(diaryContent)
                .mood(mood)
                .editedBy("AI")
                .editedAt(LocalDateTime.now())
                .build());

        AiDiary saved = aiDiaryRepository.save(d);
        return toResponse(saved);
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

    /** ✅ 일기 수정(버전 + 히스토리 적재) */
    public DiaryGenerateResponseDto updateDiary(String diaryId, String userId, DiaryUpdateRequestDto req) {
        AiDiary d = aiDiaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));

        // DRAFT & editable만 수정 허용
        if (Boolean.FALSE.equals(d.getEditable()) || !"DRAFT".equalsIgnoreCase(d.getStatus())) {
            throw new IllegalStateException("편집할 수 없는 상태입니다. (status=" + d.getStatus() + ")");
        }

        // 낙관적 락(선택)
        if (req.getExpectedVersion() != null && !Objects.equals(req.getExpectedVersion(), d.getVersion())) {
            throw new IllegalStateException("버전 충돌이 발생했습니다. 최신 내용을 새로고침 해주세요.");
        }

        // 변경값 적용 (null이면 기존 유지)
        String newTitle   = (req.getTitle()   != null) ? req.getTitle()   : d.getTitle();
        String newContent = (req.getContent() != null) ? req.getContent() : d.getContent();

        // 버전 +1
        int newVersion = (d.getVersion() == null ? 0 : d.getVersion()) + 1;
        d.setVersion(newVersion);

        d.setTitle(newTitle);
        d.setContent(newContent);
        d.setUpdatedAt(LocalDateTime.now());

        if (d.getHistory() == null) d.setHistory(new ArrayList<>());
        d.getHistory().add(DiaryEdit.builder()
                .version(newVersion)
                .title(newTitle)
                .content(newContent)
                .editedBy(userId)
                .editedAt(LocalDateTime.now())
                .build());

        AiDiary saved = aiDiaryRepository.save(d);
        return toResponse(saved);
    }

    /** ✅ 최종 저장(확정) — 더 이상 수정 불가 */
    public DiaryGenerateResponseDto finalizeDiary(DiarySaveRequestDto req, String userId) {
        AiDiary d = aiDiaryRepository.findByIdAndUserId(req.getDiaryId(), userId)
                .orElseThrow(() -> new DiaryNotFoundException(req.getDiaryId()));

        if ("FINAL".equalsIgnoreCase(d.getStatus())) {
            return toResponse(d); // 이미 확정됨
        }

        int newVersion = (d.getVersion() == null ? 0 : d.getVersion()) + 1;
        d.setVersion(newVersion);
        d.setStatus("FINAL");
        d.setEditable(false);
        d.setUpdatedAt(LocalDateTime.now());

        if (d.getHistory() == null) d.setHistory(new ArrayList<>());
        d.getHistory().add(DiaryEdit.builder()
                .version(newVersion)
                .title(d.getTitle())
                .content(d.getContent())
                .mood(d.getMood())
                .editedBy("FINALIZE:" + userId)
                .editedAt(LocalDateTime.now())
                .build());

        AiDiary saved = aiDiaryRepository.save(d);
        return toResponse(saved);
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
                .scraped(d.getScraped())
                .date(d.getDateOfDay())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
