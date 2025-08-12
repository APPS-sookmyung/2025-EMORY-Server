package emory.emoryserver.domain.user.service;

import emory.emoryserver.domain.user.dto.request.UserProfileUpdateRequest;
import emory.emoryserver.domain.user.dto.response.UserProfileResponse;
import emory.emoryserver.domain.user.entity.User;
import emory.emoryserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return new UserProfileResponse(user.getEmail(), user.getNickname());
    }

    @Override
    public UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.updateNickname(request.getNickname());
        userRepository.save(user);

        return new UserProfileResponse(user.getEmail(), user.getNickname());
    }

    @Override
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        userRepository.delete(user);
    }

    @Override
    public long countAllUsers() {
        return userRepository.count();
    }
}
