package emory.emoryserver.aidiary.repository;

import emory.emoryserver.aidiary.model.AiDiary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiDiaryRepository extends MongoRepository<AiDiary, String> {
}
