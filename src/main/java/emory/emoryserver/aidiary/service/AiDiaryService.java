package emory.emoryserver.aidiary.service;

import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.model.DiaryEdit;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import emory.emoryserver.aidiary.exception.DiaryNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiDiaryService {
    // private final ChatLogRepository ... // 세션/로그 조회 필요 시 주입
    private final AiDiaryRepository aiDiaryRepository;

    /*
    WebSocket 대화 로그 기반으로 일기 생성 및 저장
    @param chatLogs WebSocket 세션 동안의 대화 로그 (USER / AI 발화 포함)
    @param sessionId WebSocket 세션 ID (혹은 캘린더 일정 ID와 매핑 가능)
    @param userId 사용자 ID
    @return 저장된 AiDiary 객체
     */
    public AiDiary generateDiaryFromChat(List<String> chatLogs, String sessionId, String userId) {
        if (chatLogs == null || chatLogs.isEmpty()) {
            throw new IllegalArgumentException("chatLogs cannot be null or empty");
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
        //1. 대화 로그 기반으로 일기 내용 생성 (현재는 단순 합침, 추후 AI 모델 연동)
        String content = String.join("\n", chatLogs);
        String title = makeTitleFrom(content); //첫줄 20자 등의 간단 규칙
        //2. AiDiary 엔터티 생성(mood/image는 지금 null 허용)
        AiDiary diary = new AiDiary();
        diary.setSessionId(sessionId);
        diary.setUserId(userId);
        diary.setContent(content);
        diary.setMood(null);
        diary.setImageId(null);

        // 버전/상태/잠금
        diary.setVersion(1);
        diary.setStatus("DRAFT");
        diary.setEditable(true);

        // 타임 스탬프
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());

        //3. 히스토리(v1 스냅샷) 추가
        if (diary.getHistory() == null) diary.setHistory(new ArrayList<>());
        diary.getHistory().add(DiaryEdit.builder()
                .version(1)
                .title(title)
                .content(content)
                .mood(null)
                .editedAt(LocalDateTime.now())
                .editedBy("AI")
                .build());

        //4. mongoDB에 저장
        return aiDiaryRepository.save(diary);
    }
    //간단 제목 생성(임시)
    private String makeTitleFrom(String content) {
        if (content == null || content.isBlank()) return "오늘의 기록";
        String firstLine = content.strip().lines().findFirst().orElse("오늘의 기록");
        return firstLine.length() > 20 ? firstLine.substring(0, 20) + "…" : firstLine;
    }

    // 일기 수정
    public AiDiary updateDiaryContent(String diaryId, String content) {
        AiDiary diary = aiDiaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));
        diary.setContent(content);
        return aiDiaryRepository.save(diary);

    }

    /*
    저장된 일기 조회
    @param diaryId MongoDB objectId
    @return AiDiary
     */
    public AiDiary getDiaryById(String diaryId) {
        return aiDiaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));
    }
}
