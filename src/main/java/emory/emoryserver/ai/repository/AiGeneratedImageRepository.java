package emory.emoryserver.ai.repository;

import emory.emoryserver.ai.model.AiGeneratedImage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AiGeneratedImageRepository extends MongoRepository<AiGeneratedImage, String> {
    Optional<AiGeneratedImage> findByIdAndUserId(String id, String userId);
}