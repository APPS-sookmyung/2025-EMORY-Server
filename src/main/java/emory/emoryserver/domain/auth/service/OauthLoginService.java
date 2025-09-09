package emory.emoryserver.domain.auth.service;

import emory.emoryserver.domain.auth.dto.request.OauthRequestDto;
import emory.emoryserver.domain.auth.dto.response.OauthLoginResponseDto;

public interface OauthLoginService {
    OauthLoginResponseDto login(OauthRequestDto requestDto);
}
