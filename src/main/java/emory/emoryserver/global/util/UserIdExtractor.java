package emory.emoryserver.global.util;

import emory.emoryserver.domain.user.entity.User;
import emory.emoryserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserIdExtractor {

    private final UserRepository userRepository;

    /**
     * JWT에서 추출한 이메일로 사용자 MongoDB ObjectId 조회
     */
    public String getUserIdFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + email));
        return user.getId();
    }
}
