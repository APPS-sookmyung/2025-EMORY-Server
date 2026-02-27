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

    /**
     * 공개(permitAll) 경로
     * - /error: Whitelabel 루프 방지용
     * - swagger 관련: springdoc 기본 경로 포함
     * - actuator health: Cloud Run 확인용(옵션)
     */
    private static final String[] PUBLIC = {
            "/", "/ping",
            "/error",
            "/favicon.ico",
            "/robots.txt",

            // Auth
            "/api/auth/**",

            // Calendar OAuth (허용 필요)
            "/api/calendar/oauth2/authorize",
            "/api/calendar/oauth2/callback",
            "/api/calendar/google/**",

            // Actuator
            "/actuator/health", "/actuator/health/**",
            "/actuator/info",

            // Swagger (springdoc-openapi)
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF/CORS
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                // Stateless
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 기본 로그인/Basic 비활성화
                .formLogin(fl -> fl.disable())
                .httpBasic(hb -> hb.disable())

                // 예외 응답: 인증 실패 401, 권한 없음 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"message\":\"Unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"message\":\"Forbidden\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // 프리플라이트는 최우선 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ 이미지 조회는 공개(데모용) - /ai/** 보다 위에 있어야 함
                        .requestMatchers(HttpMethod.GET, "/ai/image/**").permitAll()

                        // 공개 경로
                        .requestMatchers(PUBLIC).permitAll()

                        // diary 관련 API 인증 필요
                        .requestMatchers("/aidiary/**").authenticated()
                        .requestMatchers("/diaries/**").authenticated()
                        .requestMatchers("/diary/**").authenticated()
                        .requestMatchers("/calendar/**").authenticated()
                        .requestMatchers("/timecapsule/**").authenticated()
                        .requestMatchers("/report/**").authenticated()
                        .requestMatchers("/ai/**").authenticated()
                        .requestMatchers("/api/user/**", "/api/admin/**").authenticated()

                        // 나머지 전부 보호
                        .anyRequest().authenticated()
                )

                // JWT 필터
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();

    // ✅ 허용할 프론트 도메인만 명시
    cfg.setAllowedOrigins(List.of(
            "https://apps-emory.netlify.app",
            "http://localhost:5173"
    ));
        // (권장) Authorization 헤더 노출/허용
    cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setExposedHeaders(List.of("Authorization","Content-Type"));

    // ✅ JWT 헤더 기반이면 쿠키는 안 쓰지만, 실무에서 종종 true로 둠
    // - true로 하면 allowedOrigins에 '*' 못 씀(지금은 명시 도메인이라 OK)
    cfg.setAllowCredentials(true);

    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
}

}
