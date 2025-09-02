package emory.emoryserver.aidiary.repository;

import emory.emoryserver.aidiary.model.AiDiary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AiDiaryRepository extends MongoRepository<AiDiary, String> {
    Optional<AiDiary> findTopBySessionIdOrderByCreatedAtDesc(String sessionId);
    // 수정/최종 저장시 본인것만 찾기
    Optional<AiDiary> findByIdAndUserId(String id, String userId);

}
