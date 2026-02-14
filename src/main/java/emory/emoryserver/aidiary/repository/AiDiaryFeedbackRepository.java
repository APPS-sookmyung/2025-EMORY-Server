package emory.emoryserver.aidiary.repository;

import emory.emoryserver.aidiary.model.AiDiaryFeedback;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AiDiaryFeedbackRepository extends MongoRepository<AiDiaryFeedback, String> {
    Optional<AiDiaryFeedback> findByDiaryIdAndUserId(String diaryId, String userId);
}
