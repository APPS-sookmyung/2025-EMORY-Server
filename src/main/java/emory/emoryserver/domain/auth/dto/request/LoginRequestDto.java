package emory.emoryserver.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {

    @Schema(description = "사용자 이메일", example = "user@example.com")
    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;

    @Schema(description = "사용자 비밀번호", example = "mypassword123")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
