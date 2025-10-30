package emory.emoryserver.aidiary.repository;

import emory.emoryserver.aidiary.model.AiDiary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AiDiaryRepository extends MongoRepository<AiDiary, String> {
    Optional<AiDiary> findTopBySessionIdOrderByCreatedAtDesc(String sessionId);
    // 수정/최종 저장시 본인것만 찾기
    Optional<AiDiary> findByIdAndUserId(String id, String userId);


    // 사용자별 일기 목록
    List<AiDiary> findByUserIdOrderByCreatedAtDesc(String userId);
    Page<AiDiary> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // FINAL 상태 일기만 조회 (diary 폴더용 추가)
    List<AiDiary> findByUserIdAndStatusOrderByDateOfDayDesc(String userId, String status);

    // 이미지 관련
    @Query("{'userId': ?0, 'imageId': {$ne: null, $ne: ''}}")
    List<AiDiary> findByUserIdAndImageIdIsNotNullOrderByCreatedAtDesc(String userId);

    @Query("{'userId': ?0, 'imageId': {$ne: null, $ne: ''}, 'dateOfDay': {$gte: ?1, $lt: ?2}}")
    List<AiDiary> findByUserIdAndImageIdIsNotNullAndDateOfDayBetweenOrderByCreatedAtDesc(
            String userId, LocalDate startDate, LocalDate endDate);

    // FINAL 상태 + 이미지 있는 일기 (diary 폴더용 추가)
    @Query("{'userId': ?0, 'status': ?1, 'imageId': {$ne: null, $ne: ''}}")
    List<AiDiary> findByUserIdAndStatusAndImageIdIsNotNullOrderByDateOfDayDesc(String userId, String status);

    @Query("{'userId': ?0, 'status': ?1, 'imageId': {$ne: null, $ne: ''}, 'dateOfDay': {$gte: ?2, $lt: ?3}}")
    List<AiDiary> findByUserIdAndStatusAndImageIdIsNotNullAndDateOfDayBetweenOrderByDateOfDayDesc(
            String userId, String status, LocalDate startDate, LocalDate endDate);

    // 스크랩 관련
    List<AiDiary> findByUserIdAndScrapedTrueOrderByCreatedAtDesc(String userId);
    List<AiDiary> findByUserIdAndScrapedTrueAndDateOfDayBetween(
            String userId, LocalDate startDate, LocalDate endDate);

    // 날짜 관련
    Optional<AiDiary> findByUserIdAndDateOfDay(String userId, LocalDate date);
    List<AiDiary> findByUserIdAndDateOfDayBetween(String userId, LocalDate startDate, LocalDate endDate);

    // 타임캡슐용
    @Query("{'userId': ?0, 'dateOfDay': {$gte: ?1, $lte: ?2}}")
    List<AiDiary> findByUserIdAndDateOfDayBetweenOrderByDateOfDayAsc(
            String userId, LocalDate startDate, LocalDate endDate);

    @Query("{'userId': ?0, 'dateOfDay': {$gte: ?1, $lte: ?2}, 'imageId': {$ne: null, $ne: ''}}")
    List<AiDiary> findByUserIdAndDateOfDayBetweenAndImageIdIsNotNullOrderByDateOfDayAsc(
            String userId, LocalDate startDate, LocalDate endDate);

    // 리포트용 (감정 통계)
    @Query("{'userId': ?0, 'dateOfDay': {$gte: ?1, $lte: ?2}, 'mood': {$ne: null, $ne: ''}}")
    List<AiDiary> findByUserIdAndDateOfDayBetweenAndMoodIsNotNull(
            String userId, LocalDate startDate, LocalDate endDate);

    @Query(value = "{'userId': ?0, 'dateOfDay': {$gte: ?1, $lte: ?2}, 'mood': {$ne: null}}", count = true)
    Long countByUserIdAndDateOfDayBetweenAndMoodIsNotNull(String userId, LocalDate startDate, LocalDate endDate);
}

