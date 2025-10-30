package emory.emoryserver.calendar.repository;

import emory.emoryserver.calendar.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByUserIdAndDateOrderByStartTimeAsc(String userId, LocalDate date);
    List<Event> findByUserIdAndDateBetweenOrderByDateAscStartTimeAsc(String userId, LocalDate startDate, LocalDate endDate);
    List<Event> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    Optional<Event> findByIdAndUserId(String eventId, String userId);
    Integer countByUserIdAndDate(String userId, LocalDate date);
}