package emory.emoryserver.ai.repository;

import emory.emoryserver.ai.model.ChatLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@ConditionalOnProperty(name="features.diary.enabled", havingValue="true", matchIfMissing=false)

@Repository
public interface ChatLogRepository extends MongoRepository<ChatLog, String> {
    List<ChatLog> findBySessionIdAndUserIdOrderByCreatedAtAsc(String sessionId, String userId);

    // 세션만 기준 (필요 시)
    List<ChatLog> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}