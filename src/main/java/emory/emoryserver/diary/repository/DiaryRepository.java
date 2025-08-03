package emory.emoryserver.diary.repository;

import emory.emoryserver.diary.entity.Diary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends MongoRepository<Diary, String> {
    List<Diary> findByUserIdOrderByDateDesc(String userId);
    Optional<Diary> findByUserIdAndDate(String userId, LocalDate date);
    List<Diary> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
}
