package emory.emoryserver.calendar.repository;

import emory.emoryserver.calendar.model.GoogleCalendarToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GoogleCalendarTokenRepository extends MongoRepository<GoogleCalendarToken, String> {
    Optional<GoogleCalendarToken> findByUserId(String userId);
    void deleteByUserId(String userId);
}