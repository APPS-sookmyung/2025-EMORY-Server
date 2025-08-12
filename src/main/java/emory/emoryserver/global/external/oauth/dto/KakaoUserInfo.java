package emory.emoryserver.global.external.oauth.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class KakaoUserInfo implements OauthUserInfo {

    private final String id;
    private final String email;
    private final String nickname;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.id = String.valueOf(attributes.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.email = (String) kakaoAccount.get("email");

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        this.nickname = (String) profile.get("nickname");
    }

    @Override
    public String getProviderId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getNickname() {
        return nickname;
    }
}
