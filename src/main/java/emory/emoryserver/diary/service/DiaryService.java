package emory.emoryserver.diary.service;

import emory.emoryserver.aidiary.exception.DiaryNotFoundException;
import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import emory.emoryserver.diary.dto.DiaryImage;
import emory.emoryserver.diary.dto.DiaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final AiDiaryRepository aiDiaryRepository;

    /**
     * 전체 일기 목록 조회 (FINAL 상태만, 최신순)
     */
    public List<DiaryResponse> getAllDiaries(String userId) {
        List<AiDiary> diaries = aiDiaryRepository.findByUserIdAndStatusOrderByDateOfDayDesc(userId, "FINAL");
        return diaries.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 이미지가 있는 일기 목록 조회 (FINAL 상태만, 최신순)
     */
    public List<DiaryImage> getDiaryImages(String userId, Integer year) {
        List<AiDiary> diaries;

        if (year != null) {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year + 1, 1, 1);
            diaries = aiDiaryRepository.findByUserIdAndStatusAndImageIdIsNotNullAndDateOfDayBetweenOrderByDateOfDayDesc(
                    userId, "FINAL", startDate, endDate);
        } else {
            diaries = aiDiaryRepository.findByUserIdAndStatusAndImageIdIsNotNullOrderByDateOfDayDesc(userId, "FINAL");
        }

        return diaries.stream()
                .map(this::toImageDto)
                .collect(Collectors.toList());
    }

    /**
     * 일기 삭제 (FINAL 상태만 삭제 가능)
     */
    public void deleteDiary(String diaryId, String userId) {
        AiDiary diary = aiDiaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));

        if (!"FINAL".equals(diary.getStatus())) {
            throw new IllegalStateException("최종 저장되지 않은 일기는 삭제할 수 없습니다.");
        }

        aiDiaryRepository.delete(diary);
    }


    /**
     * 스크랩 토글
     */
    public DiaryResponse toggleScrap(String diaryId, String userId) {
        AiDiary diary = aiDiaryRepository.findByIdAndUserId(diaryId, userId)
                .orElseThrow(() -> new DiaryNotFoundException(diaryId));

        // 스크랩 상태 토글
        diary.setScraped(!Boolean.TRUE.equals(diary.getScraped()));
        diary.setUpdatedAt(LocalDateTime.now());

        AiDiary updatedDiary = aiDiaryRepository.save(diary);
        return toResponseDto(updatedDiary);
    }

    // AiDiary를 DiaryResponse로 변환
    private DiaryResponse toResponseDto(AiDiary diary) {
        String content = null;
        if (diary.getContent() != null) {
            content = diary.getContent().length() > 100
                    ? diary.getContent().substring(0, 100) + "..."
                    : diary.getContent();
        }

        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .title(diary.getTitle())
                .content(content)
                .emotion(diary.getMood())
                .imageId(diary.getImageId())
                .scraped(diary.getScraped())
                .status(diary.getStatus())
                .date(diary.getDateOfDay())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }

    // AiDiary를 DiaryImage로 변환
    private DiaryImage toImageDto(AiDiary diary) {
        return DiaryImage.builder()
                .diaryId(diary.getId())
                .imageId(diary.getImageId())
                .date(diary.getDateOfDay())
                .build();
    }
}