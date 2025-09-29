package emory.emoryserver.global.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true) // @PermitAll 사용 가능
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * ★ 완전 제외(ignore): 이 경로들은 Spring Security 필터를 아예 타지 않음.
     *   (디버그/안정화용으로 강력. 이후 필요 시 permitAll로 단계적 전환 가능)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/ping",
                "/ai/chat/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/favicon.ico",
                "/actuator/health",
                "/actuator/info"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 위의 ignoring()으로 제외된 것 외 나머지는 정책 적용
                        .anyRequest().authenticated()
                )
                .anonymous(Customizer.withDefaults())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedJson()));

        // JWT 필터 등록(인증 필요 경로만 대상이 됨; ignore된 경로에는 적용 안 됨)
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint unauthorizedJson() {
        return (req, res, e) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"" + (e != null ? e.getMessage() : "") + "\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "https://emory-server-406346608321.asia-northeast3.run.app",
                "http://localhost:3000",
                "http://localhost:5173"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization","Content-Type"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
