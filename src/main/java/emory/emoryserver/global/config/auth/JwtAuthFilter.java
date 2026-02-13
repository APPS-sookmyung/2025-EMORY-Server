package emory.emoryserver.global.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private static final AntPathMatcher matcher = new AntPathMatcher();

    // SecurityConfig.PUBLIC 과 동일하게 유지 (경로 한 글자라도 다르면 스킵 실패)
    private static final List<String> WHITELIST = List.of(
            "/", "/ping",
            "/api/auth/**",
            "/actuator/health", "/actuator/info",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/swagger-resources/**", "/webjars/**",
            "/favicon.ico"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 프리플라이트는 무조건 스킵
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String path = request.getRequestURI(); // 컨텍스트패스 없는지 확인
        for (String p : WHITELIST) {
            if (matcher.match(p, path)) return true; // 공개 경로는 필터 스킵
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            String auth = req.getHeader("Authorization");

            // 토큰 없으면 인증 시도 없이 통과 → 최종 authorize 단계에서 공개/보호 구분
            if (auth == null || !auth.startsWith("Bearer ")) {
                chain.doFilter(req, res);
                return;
            }

            String token = auth.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                var authObj = jwtTokenProvider.getAuthentication(token); // Authentication 또는 UserDetails

                if (authObj instanceof org.springframework.security.core.Authentication authentication) {

                    // details 세팅은 AbstractAuthenticationToken일 때만 가능
                    if (authentication instanceof org.springframework.security.authentication.AbstractAuthenticationToken aat) {
                        aat.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    }

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else if (authObj instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            }
            // 토큰 무효여도 여기서 sendError/redirect 하지 않음 → EntryPoint가 401 처리

            chain.doFilter(req, res);
        } catch (Exception ex) {
            // 예외가 나도 필터에서 직접 401/403을 쓰지 말고 체인 진행
            // (원인 추적 위해 로깅만)
            // log.warn("JWT filter error", ex);
            chain.doFilter(req, res);
        }
    }
}
