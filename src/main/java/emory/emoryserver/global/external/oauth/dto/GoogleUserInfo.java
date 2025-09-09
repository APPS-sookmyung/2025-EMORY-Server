package emory.emoryserver.global.external.oauth.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class GoogleUserInfo implements OauthUserInfo {

    private final String id;
    private final String email;
    private final String name;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.id = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
        this.name = (String) attributes.get("name");
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
        return name;
    }
}
