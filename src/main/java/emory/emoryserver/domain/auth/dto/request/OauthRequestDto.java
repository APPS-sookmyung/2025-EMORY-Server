package emory.emoryserver.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OauthRequestDto {

    @Schema(description = "SNS access token", example = "ya29.a0AfH6...")
    @NotBlank(message = "accessToken은 필수입니다.")
    private String accessToken;

    @Schema(description = "provider 이름 (google / kakao)", example = "google")
    @NotBlank(message = "provider는 필수입니다.")
    private String provider;
}
