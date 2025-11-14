package emory.emoryserver.domain.user.repository;

import emory.emoryserver.domain.user.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

<<<<<<< HEAD
    // 이메일로 조회
    Optional<User> findByEmail(String email);

    // OAuth 제공자 + 제공자 ID로 조회 (예: GOOGLE / KAKAO 등)
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 존재 여부 체크
    boolean existsByEmail(String email);
    boolean existsByProviderAndProviderId(String provider, String providerId);

    // 리마인더 켜둔 사용자 목록 (예: 예약 전송/스케줄러에서 사용)
    List<User> findAllByDiaryReminderEnabledTrue();
=======
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
}
