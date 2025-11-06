package emory.emoryserver.timecapsule.service;

import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import emory.emoryserver.timecapsule.dto.TimecapsuleResponseDto;
import emory.emoryserver.timecapsule.dto.TimecapsuleImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimecapsuleService {

    private final AiDiaryRepository aiDiaryRepository;

    /**
     * 작년 같은 주의 타임캡슐 조회
     * 오늘 기준으로 작년 같은 주(일~토)의 일기들을 요약
     */
    public TimecapsuleResponseDto getTimecapsule(String userId) {
        LocalDate today = LocalDate.now();
        LocalDate lastYearToday = today.minusYears(1);

        // 작년 같은 주의 일요일과 토요일 계산
        LocalDate weekStart = lastYearToday.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate weekEnd = lastYearToday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));

        // 해당 주의 모든 일기 조회
        List<AiDiary> weekDiaries = aiDiaryRepository.findByUserIdAndDateOfDayBetweenOrderByDateOfDayAsc(
                userId, weekStart, weekEnd);

        if (weekDiaries.isEmpty()) {
            return TimecapsuleResponseDto.builder()
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .weekSummary("작년 이 주에는 작성된 일기가 없습니다.")
                    .images(List.of())
                    .diaryCount(0)
                    .build();
        }

        // AI 요약 생성
        String weekSummary = generateWeekSummary(weekDiaries, weekStart, weekEnd);

        // 이미지가 있는 일기들만 추출
        List<AiDiary> diariesWithImages = weekDiaries.stream()
                .filter(diary -> diary.getImageId() != null && !diary.getImageId().isEmpty())
                .collect(Collectors.toList());

        List<TimecapsuleImageDto> images = diariesWithImages.stream()
                .map(this::toTimecapsuleImageDto)
                .collect(Collectors.toList());

        return TimecapsuleResponseDto.builder()
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .weekSummary(weekSummary)
                .images(images)
                .diaryCount(weekDiaries.size())
                .build();
    }

    /**
     * 주간 일기 요약 생성
     * 실제 프로덕션에서는 AI 서비스(GPT, Claude 등)를 호출하여 요약 생성
     */
    private String generateWeekSummary(List<AiDiary> diaries, LocalDate weekStart, LocalDate weekEnd) {
        if (diaries.isEmpty()) {
            return "이 주에는 특별한 기록이 없었습니다.";
        }

        // 감정 분석
        List<String> emotions = diaries.stream()
                .map(AiDiary::getMood)
                .filter(mood -> mood != null && !mood.isEmpty())
                .collect(Collectors.toList());

        String dominantEmotion = findDominantEmotion(emotions);

        // 간단한 요약 생성
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("작년 %d월 %d일부터 %d일까지의 한 주간, ",
                weekStart.getMonthValue(), weekStart.getDayOfMonth(), weekEnd.getDayOfMonth()));
        summary.append(String.format("총 %d개의 일기를 작성하셨습니다. ", diaries.size()));

        if (dominantEmotion != null) {
            summary.append(String.format("이 주에는 주로 '%s'의 감정을 많이 느끼셨던 것 같네요. ", dominantEmotion));
        }

        // 주요 키워드 추출 (간단한 방식)
        String keywordSummary = extractKeywords(diaries);
        if (!keywordSummary.isEmpty()) {
            summary.append(keywordSummary);
        }

        summary.append("소중한 추억들을 다시 만나보세요!");

        return summary.toString();
    }

    /**
     * 주요 감정 찾기
     */
    private String findDominantEmotion(List<String> emotions) {
        if (emotions.isEmpty()) return null;

        return emotions.stream()
                .collect(Collectors.groupingBy(emotion -> emotion, Collectors.counting()))
                .entrySet()
                .stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 키워드 추출 (간단한 방식)
     */
    private String extractKeywords(List<AiDiary> diaries) {
        String[] commonWords = {"행복", "기쁨", "슬픔", "화남", "걱정", "스트레스", "사랑", "친구", "가족", "일", "여행"};

        String allContent = diaries.stream()
                .map(diary -> diary.getTitle() + " " + diary.getContent())
                .collect(Collectors.joining(" "));

        List<String> foundKeywords = List.of(commonWords).stream()
                .filter(keyword -> allContent.contains(keyword))
                .limit(3)
                .collect(Collectors.toList());

        if (!foundKeywords.isEmpty()) {
            return String.format("'%s' 등에 대한 이야기가 많았네요. ", String.join("', '", foundKeywords));
        }

        return "";
    }

    /**
     * AiDiary를 TimecapsuleImageDto로 변환
     */
    private TimecapsuleImageDto toTimecapsuleImageDto(AiDiary diary) {
        return TimecapsuleImageDto.builder()
                .diaryId(diary.getId())
                .imageId(diary.getImageId())
                .title(diary.getTitle())
                .date(diary.getDateOfDay())
                .build();
    }
}