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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 공개 엔드포인트
                        .requestMatchers(
                                "/", "/error", "/ping",
                                "/actuator/health", "/actuator/health/**", "/actuator/info",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
                                "/webjars/**", "/favicon.ico",
                                "/ai/chat/**"
                        ).permitAll()

                        // 로그인/인증 진입점은 반드시 허용
                        .requestMatchers("/api/auth/**").permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedJson())
                        .accessDeniedHandler(accessDeniedJson())
                )
                .anonymous(Customizer.withDefaults());

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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(
                "https://*.run.app",
                "https://*.a.run.app",
                "https://*.cloud.run",
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));
        cfg.setAllowCredentials(false);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization","Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
