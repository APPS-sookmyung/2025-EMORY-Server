package emory.emoryserver.aidiary.service;

import emory.emoryserver.aidiary.dto.FeedbackSaveRequestDto;
import emory.emoryserver.aidiary.dto.FeedbackSaveResponseDto;
import emory.emoryserver.aidiary.exception.DiaryNotFoundException;
import emory.emoryserver.aidiary.model.AiDiaryFeedback;
import emory.emoryserver.aidiary.repository.AiDiaryFeedbackRepository;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final AiDiaryRepository aiDiaryRepository;
    private final AiDiaryFeedbackRepository feedbackRepository;

    public FeedbackSaveResponseDto saveOrUpdate(String diaryId, String userId, FeedbackSaveRequestDto req) {
        if (diaryId == null || diaryId.isBlank()) throw new IllegalArgumentException("diaryId는 필수입니다.");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId는 필수입니다.");
        if (req == null || req.getSelectedOption() == null || req.getSelectedOption().isBlank())
            throw new IllegalArgumentException("selectedOption은 필수입니다.");

        // ✅ 내 일기인지 확인 (owner만 피드백 저장하도록)
        aiDiaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));

        LocalDateTime now = LocalDateTime.now();

        AiDiaryFeedback saved = feedbackRepository.findByDiaryIdAndUserId(diaryId, userId)
                .map(existing -> {
                    existing.setSelectedOption(req.getSelectedOption());
                    existing.setUpdatedAt(now);
                    return feedbackRepository.save(existing);
                })
                .orElseGet(() -> feedbackRepository.save(
                        AiDiaryFeedback.builder()
                                .diaryId(diaryId)
                                .userId(userId)
                                .selectedOption(req.getSelectedOption())
                                .createdAt(now)
                                .updatedAt(now)
                                .build()
                ));

        return FeedbackSaveResponseDto.builder()
                .feedbackId(saved.getId())
                .diaryId(saved.getDiaryId())
                .selectedOption(saved.getSelectedOption())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
