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
    private final TimecapsuleAiGenerator timecapsuleAiGenerator;

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
        String weekSummary = timecapsuleAiGenerator.generateWeeklySummary(weekDiaries, weekStart, weekEnd);

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