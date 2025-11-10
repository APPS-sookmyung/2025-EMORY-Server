/*

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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiDiaryService {
    // private final ChatLogRepository ... // 세션/로그 조회 필요 시 주입
    private final AiDiaryRepository aiDiaryRepository;
    private final ChatLogRepository chatLogRepository;

    /**
     * 1) DB 저장 대화로그 기반 생성 (실제 사용 버전)
     */
/*
    public DiaryGenerateResponseDto generateDiaryFromSession(@Valid DiaryGenerateRequestDto req, String userId) {
        if (req.getSessionId() == null || req.getSessionId().isBlank()) {
            throw new IllegalArgumentException("sessionId는 필수입니다.");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId는 필수입니다.");
        }

        // (sessionId,userId) 기준으로 시간 오름차순 조회 (메서드명은 네 리포지토리에 맞춰 변경)
        List<ChatLog> logs = chatLogRepository
                .findBySessionIdAndUserIdOrderByCreatedAtAsc(req.getSessionId(), userId);

        if (logs.isEmpty())
            throw new IllegalArgumentException("대화 로그가 없습니다. (sessionId=" + req.getSessionId() + ", userId=" + userId + ")");


        // USER/AI 모두 포함해 content 구성
        String content = logs.stream()
                .map(m -> prefix(m.getRole()) + safe(m.getText()))
                .collect(Collectors.joining("\n"));
        // 너무 길면 컷
        if (content.length() > 10_000) content = content.substring(0, 10_000) + " …";

        // 제목: 첫 줄 20자
        String title = makeTitleFrom(content);

        // 일기 날짜: 요청값 or 마지막 메시지 시각 or 오늘
        LocalDate day = req.getDateOfDay() != null
                ? req.getDateOfDay()
                : (logs.get(logs.size() - 1).getCreatedAt() != null
                    ? logs.get(logs.size() - 1).getCreatedAt().toLocalDate()
                    : LocalDate.now());

        // DRAFT v1 저장 + history 스냅샷
        AiDiary d = new AiDiary();
        d.setSessionId(req.getSessionId());
        d.setUserId(userId);
        d.setTitle(title);                 // ✅ 누락되어 있던 부분 추가
        d.setContent(content);
        d.setMood(null);
        d.setImageId(null);
        d.setScraped(false);
        d.setVersion(1);
        d.setStatus("DRAFT");
        d.setEditable(true);
        d.setDateOfDay(day);
        d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());

        d.setHistory(new ArrayList<>(List.of(
                DiaryEdit.builder().version(1).title(title).content(content).editedBy("AI")
                        .editedAt(LocalDateTime.now()).build()
        )));
        d.getHistory().add(DiaryEdit.builder()
                .version(1)
                .title(title)
                .content(content)
                .mood(null)
                .editedAt(LocalDateTime.now())
                .editedBy("AI")
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

 */

    /** ✅ 일기 수정(버전 + 히스토리 적재) */
/*
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
/*
    public DiaryGenerateResponseDto finalizeDiary(DiarySaveRequestDto req, String userId) {
        AiDiary d = aiDiaryRepository.findByIdAndUserId(req.getDiaryId(), userId)
                .orElseThrow(() -> new DiaryNotFoundException(req.getDiaryId()));

        if ("FINAL".equalsIgnoreCase(d.getStatus())) {
            return toResponse(d); // 이미 확정됨
        }


        // (선택) 대표 이미지 고정까지 함께 처리하려면 주석 해제
        // if (req.getPrimaryImageId() != null) {
        //     d.setPrimaryImageId(req.getPrimaryImageId());
        // }

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

*/