package emory.emoryserver.ai.repository;

import emory.emoryserver.ai.model.ImageAsset;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ImageAssetRepository extends MongoRepository<ImageAsset, String> {
    Optional<ImageAsset> findTopBySessionIdOrderByCreatedAtDesc(String sessionId);
}
