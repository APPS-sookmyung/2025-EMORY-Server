package emory.emoryserver.ai.repository;

import emory.emoryserver.ai.model.ChatLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatLog, String> {
    List<ChatLog> findBysessionIdAndUserIdOrderByCreatedAtAsc(String sessionId, String userId);
}
