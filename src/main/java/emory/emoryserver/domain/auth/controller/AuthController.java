package emory.emoryserver.domain.auth.controller;

import emory.emoryserver.domain.auth.dto.request.OauthRequestDto;
import emory.emoryserver.domain.auth.dto.response.OauthLoginResponseDto;
import emory.emoryserver.domain.auth.service.OauthLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OauthLoginService oauthLoginService;

    // SNS 로그인 API (Google / Kakao)
    @PostMapping("/oauth")
    public ResponseEntity<OauthLoginResponseDto> oauthLogin(
            @RequestBody @Valid OauthRequestDto requestDto) {
        OauthLoginResponseDto responseDto = oauthLoginService.login(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
