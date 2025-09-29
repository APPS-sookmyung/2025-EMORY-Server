package emory.emoryserver.global.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API라면 보통 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 문서/테스트용 공개 엔드포인트
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        // (필요하면 여기에 추가 공개 경로를 넣으세요)
                        // .requestMatchers("/auth/oauth", "/actuator/health").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 기본 인증(예: 임시로 테스트 시)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
