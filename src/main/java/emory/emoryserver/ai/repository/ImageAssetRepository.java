package emory.emoryserver.ai.repository;

import emory.emoryserver.ai.model.ImageAsset;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageAssetRepository extends MongoRepository<ImageAsset, String> {
}
