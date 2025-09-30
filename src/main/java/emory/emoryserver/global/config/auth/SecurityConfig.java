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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Spring Security 6 스타일.
     * Swagger / docs / ping / actuator 등은 항상 공개.
     * 그 외는 인증 필요.
     * 403 이슈 방지를 위해 CORS, 예외 핸들러 정리.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API 기본 세팅
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 권한
                .authorizeHttpRequests(auth -> auth
                        // Preflight 전면 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 엔드포인트
                        .requestMatchers(
                                "/",
                                "/error",
                                "/ping",
                                "/actuator/health",
                                "/actuator/info",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/ai/chat/**"
                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // 예외 응답(JSON)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedJson()) // 401
                        .accessDeniedHandler(accessDeniedJson())      // 403
                )

                // 익명 허용
                .anonymous(Customizer.withDefaults());

        // JWT 필터 (UsernamePasswordAuthenticationFilter 앞)
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint unauthorizedJson() {
        return (req, res, e) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Unauthorized";
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"" + msg + "\"}");
        };
    }

    private AccessDeniedHandler accessDeniedJson() {
        return (req, res, e) -> {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Forbidden";
            res.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"" + msg + "\"}");
        };
    }

    /**
     * CORS
     * - Swagger UI(동일 도메인)에는 영향이 거의 없지만,
     *   프론트(로컬/다른 도메인)에서 호출할 때를 대비해 설정.
     * - 필요 시 allowedOriginPatterns로 운영 도메인 추가.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 운영/개발 도메인들(패턴 허용; Cloud Run 도메인/커스텀 도메인/로컬)
        cfg.setAllowedOriginPatterns(List.of(
                "https://*.run.app",
                "https://*.a.run.app",
                "https://*.cloud.run",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        // 자격증명 필요 없으면 false 권장(와일드카드 패턴과 충돌 방지)
        cfg.setAllowCredentials(false);

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
