package emory.emoryserver.calendar.service;

import emory.emoryserver.aidiary.model.AiDiary;
import emory.emoryserver.aidiary.repository.AiDiaryRepository;
import emory.emoryserver.calendar.dto.*;
import emory.emoryserver.calendar.model.Event; // 로컬 Event
import emory.emoryserver.calendar.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final AiDiaryRepository aiDiaryRepository;
    private final EventRepository eventRepository;
    private final GoogleCalendarService googleCalendarService;

    public CalendarResponseDto getMonthlyCalendar(String userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AiDiary> diaries = aiDiaryRepository.findByUserIdAndDateOfDayBetween(userId, startDate, endDate);
        Map<LocalDate, AiDiary> diaryMap = diaries.stream()
                .collect(Collectors.toMap(AiDiary::getDateOfDay, diary -> diary));

        List<CalendarDateDto> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            AiDiary diary = diaryMap.get(date);

            CalendarDateDto dateDto = CalendarDateDto.builder()
                    .date(date)
                    .emotion(diary != null ? diary.getMood() : null)
                    .hasDiary(diary != null)
                    .isScraped(diary != null ? diary.getScraped() : false)
                    .build();

            dates.add(dateDto);
        }

        return CalendarResponseDto.builder()
                .year(year)
                .month(month)
                .dates(dates)
                .build();
    }

    public CalendarResponseDto getScrapedCalendar(String userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AiDiary> scrapedDiaries = aiDiaryRepository.findByUserIdAndScrapedTrueAndDateOfDayBetween(
                userId, startDate, endDate);
        Map<LocalDate, AiDiary> scrapedDiaryMap = scrapedDiaries.stream()
                .collect(Collectors.toMap(AiDiary::getDateOfDay, diary -> diary));

        // ⚠️ 여기 수정: Event는 로컬 Event
        List<Event> events = eventRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        Map<LocalDate, Long> eventCountMap = events.stream()
                .collect(Collectors.groupingBy(Event::getDate, Collectors.counting()));

        List<CalendarDateDto> dates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            AiDiary scrapedDiary = scrapedDiaryMap.get(date);
            Long eventCount = eventCountMap.getOrDefault(date, 0L);

            CalendarDateDto dateDto = CalendarDateDto.builder()
                    .date(date)
                    .emotion(scrapedDiary != null ? scrapedDiary.getMood() : null)
                    .hasDiary(scrapedDiary != null)
                    .isScraped(scrapedDiary != null)
                    .build();

            dates.add(dateDto);
        }

        return CalendarResponseDto.builder()
                .year(year)
                .month(month)
                .dates(dates)
                .build();
    }

    /**
     * 특정 날짜 상세 조회 (일기 + 직접 생성 일정 + 구글 캘린더 일정)
     */
    public DateDetailResponseDto getDateDetail(String userId, LocalDate date) {
        AiDiary diary = aiDiaryRepository.findByUserIdAndDateOfDay(userId, date).orElse(null);
        DiaryDetailDto diaryDetail = null;

        if (diary != null) {
            diaryDetail = DiaryDetailDto.builder()
                    .diaryId(diary.getId())
                    .title(diary.getTitle())
                    .content(diary.getContent())
                    .emotion(diary.getMood())
                    .imageId(diary.getImageId())
                    .scraped(diary.getScraped())
                    .build();
        }

        // 직접 생성한 일정
        List<Event> userEvents = eventRepository.findByUserIdAndDateOrderByStartTimeAsc(userId, date);
        List<EventResponseDto> eventDtos = userEvents.stream()
                .map(this::toEventResponseDto)
                .collect(Collectors.toList());

        // 구글 캘린더 일정 (연동된 경우에만)
        if (googleCalendarService.isConnected(userId)) {
            // ⚠️ 여기는 구글 Event를 전체 경로로 사용
            List<com.google.api.services.calendar.model.Event> googleEvents =
                    googleCalendarService.getEventsByDate(userId, date);
            List<EventResponseDto> googleEventDtos = googleEvents.stream()
                    .map(this::googleEventToResponseDto)
                    .collect(Collectors.toList());
            eventDtos.addAll(googleEventDtos);
        }

        // 시간순 정렬
        eventDtos.sort((a, b) -> {
            if (a.getStartTime() == null && b.getStartTime() == null) return 0;
            if (a.getStartTime() == null) return 1;
            if (b.getStartTime() == null) return -1;
            return a.getStartTime().compareTo(b.getStartTime());
        });

        return DateDetailResponseDto.builder()
                .date(date)
                .diary(diaryDetail)
                .events(eventDtos)
                .build();
    }

    // 구글 이벤트 변환 (전체 경로 사용)
    private EventResponseDto googleEventToResponseDto(
            com.google.api.services.calendar.model.Event googleEvent) {
        LocalDateTime startDateTime = null;
        if (googleEvent.getStart() != null && googleEvent.getStart().getDateTime() != null) {
            startDateTime = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(googleEvent.getStart().getDateTime().getValue()),
                    java.time.ZoneId.systemDefault());
        }

        return EventResponseDto.builder()
                .eventId("google_" + googleEvent.getId())
                .title(googleEvent.getSummary())
                .date(startDateTime != null ? startDateTime.toLocalDate() : null)
                .startTime(startDateTime != null ? startDateTime.toLocalTime() : null)
                .description(googleEvent.getDescription())
                .createdAt(null)
                .build();
    }

    // 로컬 이벤트 생성
    public EventResponseDto createEvent(String userId, EventRequestDto request) {
        Event event = Event.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .date(request.getDate())
                .startTime(request.getStartTime())
                .eventType("USER_CREATED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Event savedEvent = eventRepository.save(event);
        return toEventResponseDto(savedEvent);
    }

    public void deleteEvent(String userId, String eventId) {
        Event event = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다: " + eventId));

        eventRepository.delete(event);
    }



    // 로컬 이벤트 변환
    private EventResponseDto toEventResponseDto(Event event) {
        return EventResponseDto.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .date(event.getDate())
                .startTime(event.getStartTime())
                .description(event.getDescription())
                .createdAt(event.getCreatedAt())
                .build();
    }
}