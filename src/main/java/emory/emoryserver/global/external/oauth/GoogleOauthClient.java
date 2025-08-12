package emory.emoryserver.global.external.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import emory.emoryserver.global.external.oauth.dto.GoogleUserInfo;
import emory.emoryserver.global.external.oauth.dto.OauthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleOauthClient {

    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public OauthUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_USER_INFO_URL,
                HttpMethod.GET,
                request,
                String.class
        );

        try {
            Map<String, Object> attributes = objectMapper.readValue(response.getBody(), Map.class);
            return new GoogleUserInfo(attributes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Google user info", e);
        }
    }
}
