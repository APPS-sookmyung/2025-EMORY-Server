package emory.emoryserver.aidiary.repository;

import emory.emoryserver.aidiary.model.AiDiary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AiDiaryRepository extends MongoRepository<AiDiary, String> {
}
