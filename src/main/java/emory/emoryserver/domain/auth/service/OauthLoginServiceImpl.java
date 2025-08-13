package emory.emoryserver.domain.auth.service;

import emory.emoryserver.domain.auth.dto.request.OauthRequestDto;
import emory.emoryserver.domain.auth.dto.response.OauthLoginResponseDto;
import emory.emoryserver.domain.user.entity.User;
import emory.emoryserver.domain.user.repository.UserRepository;
import emory.emoryserver.global.external.oauth.GoogleOauthClient;
import emory.emoryserver.global.external.oauth.KakaoOauthClient;
import emory.emoryserver.global.external.oauth.dto.OauthUserInfo;
import emory.emoryserver.global.config.auth.JwtTokenProvider;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OauthLoginServiceImpl implements OauthLoginService {

    private final GoogleOauthClient googleOauthClient;
    private final KakaoOauthClient kakaoOauthClient;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public OauthLoginResponseDto login(OauthRequestDto requestDto) {
        OauthUserInfo userInfo;

        // 1. Provider에 따라 사용자 정보 요청
        switch (requestDto.getProvider().toLowerCase()) {
            case "google":
                userInfo = googleOauthClient.getUserInfo(requestDto.getAccessToken());
                break;
            case "kakao":
                userInfo = kakaoOauthClient.getUserInfo(requestDto.getAccessToken());
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다: " + requestDto.getProvider());
        }

        // 2. 이메일 기준으로 기존 사용자 검색
        User user = userRepository.findByEmail(userInfo.getEmail()).orElse(null);
        boolean isNewUser = false;

        // 3. 신규 사용자면 회원가입 처리
        if (user == null) {
            user = User.builder()
                    .provider(requestDto.getProvider())
                    .providerId(userInfo.getProviderId())
                    .email(userInfo.getEmail())
                    .nickname(userInfo.getNickname())
                    .build();

            userRepository.save(user);
            isNewUser = true;
        }

        // 4. JWT 발급 (email을 subject로)
        String token = jwtTokenProvider.createToken(
                user.getEmail(),
                List.of("ROLE_USER")
        );

        // 5. 응답 반환
        return new OauthLoginResponseDto(token, isNewUser);
    }
}
