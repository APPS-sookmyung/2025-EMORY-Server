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

    // 🔓 공개 경로 (context-path 유무와 무관하게 매칭되도록 구성)
    private static final List<String> WHITELIST = List.of(
            "/",
            "/ping",               // ★ 추가
            "/ai/chat/**",         // ★ 추가
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/favicon.ico",
            "/actuator/health",
            "/actuator/info"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // ★ getServletPath() 사용: /api 같은 context-path가 있어도 정상 매칭
        String path = request.getServletPath();
        return WHITELIST.stream().anyMatch(p -> matcher.match(p, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws IOException, ServletException {

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        try {
            String token = jwtTokenProvider.resolveToken(req);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                var authentication = jwtTokenProvider.getAuthentication(token);
                if (authentication instanceof UsernamePasswordAuthenticationToken authenticationToken) {
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (Exception ignored) {
            // 토큰 문제는 그냥 인증 미적용으로 두고 다음 필터로 넘김
        }

        chain.doFilter(req, res);
    }
}
