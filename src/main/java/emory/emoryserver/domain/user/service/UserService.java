package emory.emoryserver.domain.user.service;

<<<<<<< HEAD
import emory.emoryserver.domain.user.dto.request.UserProfileUpdateRequest;
import emory.emoryserver.domain.user.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getMyProfile(String userId);
    UserProfileResponse updateProfile(String userId, UserProfileUpdateRequest request);
    void deleteAccount(String userId); // 회원 탈퇴
    long countAllUsers();

=======
import emory.emoryserver.domain.auth.dto.request.SignupRequestDto;
import emory.emoryserver.domain.user.entity.User;
import emory.emoryserver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = new User(
                requestDto.getEmail(),
                encodedPassword,
                requestDto.getNickname()
        );

        userRepository.save(user);
    }
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
}
