package emory.emoryserver.global.config.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    // Swagger & 공개 경로 화이트리스트
    private static final String[] PUBLIC = {
            "/", "/ping",
            "/api/auth/**",                // <- 실제 AuthController 경로와 일치
            "/actuator/health", "/actuator/info",

            // springdoc-openapi / swagger
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF/CORS
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                // 세션 완전 비활성(쿠키 생성 방지)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 기본 로그인/HTTP Basic 끄기(예상치 못한 403/리다이렉트 방지)
                .formLogin(fl -> fl.disable())
                .httpBasic(hb -> hb.disable())

                // 예외 전략: 무토큰 접근은 401, 권한없음은 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )

                // 권한 규칙
                .authorizeHttpRequests(auth -> auth
                        // 프리플라이트 전면 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 경로 허용 (anyRequest보다 반드시 먼저)
                        .requestMatchers(PUBLIC).permitAll()

                        // 그 외 보호
                        .anyRequest().authenticated()
                )

                // JWT 필터 연결: UsernamePasswordAuthenticationFilter 앞
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS (필요시 origin을 구체 도메인으로 제한)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(
                "*"
        ));
        cfg.setAllowCredentials(false); // 쿠키 안 쓰면 false 권장
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization","Content-Type"));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
