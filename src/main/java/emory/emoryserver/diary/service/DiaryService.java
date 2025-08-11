package emory.emoryserver.diary.service;

import emory.emoryserver.diary.dto.DiaryCreateRequest;
import emory.emoryserver.diary.dto.DiaryImageResponse;
import emory.emoryserver.diary.dto.DiaryListResponse;
import emory.emoryserver.diary.dto.DiaryResponse;
import emory.emoryserver.diary.dto.DiaryUpdateRequest;
import emory.emoryserver.diary.entity.Diary;
import emory.emoryserver.diary.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;

    public DiaryListResponse getDiaryList(String userId) {
        List<Diary> diaries = diaryRepository.findByUserIdOrderByDateDesc(userId);
        List<DiaryResponse> diaryResponses = diaries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        DiaryListResponse response = new DiaryListResponse();
        response.setDiaries(diaryResponses);
        response.setCanWriteToday(!diaryRepository.findByUserIdAndDate(userId, LocalDate.now()).isPresent());
        response.setTodayDate(LocalDate.now().toString());

        return response;
    }

    public List<DiaryImageResponse> getDiaryImages(String userId) {
        List<Diary> diaries = diaryRepository.findByUserIdOrderByDateDesc(userId);
        return diaries.stream()
                .filter(diary -> diary.getAiImageUrl() != null && !diary.getAiImageUrl().isEmpty())
                .map(this::convertToImageResponse)
                .collect(Collectors.toList());
    }

    public DiaryResponse createDiary(String userId, DiaryCreateRequest request) {
        // 하루에 하나의 일기만 작성 가능
        if (diaryRepository.findByUserIdAndDate(userId, request.getDate()).isPresent()) {
            throw new IllegalArgumentException("해당 날짜에 이미 일기가 작성되었습니다.");
        }

        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setDate(request.getDate());
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setEmotionCategory(request.getEmotionCategory());
        diary.setAiImageUrl(request.getAiImageUrl());

        Diary savedDiary = diaryRepository.save(diary);
        return convertToResponse(savedDiary);
    }

    public DiaryResponse updateDiary(String userId, String diaryId, DiaryUpdateRequest request) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (!diary.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // 수정 가능한 필드들만 업데이트 (제목, 내용, 감정 카테고리)
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setEmotionCategory(request.getEmotionCategory());

        Diary updatedDiary = diaryRepository.save(diary);
        return convertToResponse(updatedDiary);
    }

    public void deleteDiary(String userId, String diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (!diary.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        diaryRepository.delete(diary);
    }

    public DiaryResponse toggleScrap(String userId, String diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new IllegalArgumentException("일기를 찾을 수 없습니다."));

        if (!diary.getUserId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        diary.setScraped(!diary.isScraped());
        Diary updatedDiary = diaryRepository.save(diary);
        return convertToResponse(updatedDiary);
    }

    private DiaryResponse convertToResponse(Diary diary) {
        DiaryResponse response = new DiaryResponse();
        response.setId(diary.getId());
        response.setDate(diary.getDate());
        response.setTitle(diary.getTitle());
        response.setContent(diary.getContent());
        response.setEmotionCategory(diary.getEmotionCategory());
        response.setAiImageUrl(diary.getAiImageUrl());
        response.setScraped(diary.isScraped());
        response.setCreatedAt(diary.getCreatedAt());
        response.setUpdatedAt(diary.getUpdatedAt());
        return response;
    }

    private DiaryImageResponse convertToImageResponse(Diary diary) {
        DiaryImageResponse response = new DiaryImageResponse();
        response.setId(diary.getId());
        response.setDate(diary.getDate());
        response.setAiImageUrl(diary.getAiImageUrl());
        return response;
    }
}