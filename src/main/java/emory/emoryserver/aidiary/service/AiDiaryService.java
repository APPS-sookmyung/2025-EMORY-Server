package emory.emoryserver.aidiary.service;

import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import emory.emoryserver.aidiary.exception.DiaryNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiDiaryService {

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
        //2. AiDiary 엔터티 생성
        AiDiary diary = new AiDiary();
        diary.setSessionId(sessionId);
        diary.setUserId(userId);
        diary.setContent(content);
        diary.setCreatedAt(LocalDateTime.now());
        //3. mongoDB에 저장
        return aiDiaryRepository.save(diary);

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
