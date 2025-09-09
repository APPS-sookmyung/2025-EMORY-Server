package emory.emoryserver.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OauthLoginResponseDto {
    private String accessToken;
    private boolean isNewUser;
}
