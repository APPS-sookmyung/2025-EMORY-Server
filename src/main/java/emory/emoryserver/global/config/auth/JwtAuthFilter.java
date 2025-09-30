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

    // 컨텍스트 경로(/api) 유무 모두 커버하도록 패턴 구성
    private static final List<String> WHITELIST = List.of(
            "/", "/error",
            "/ping", "/api/ping",
            "/ai/chat/**",
            "/api/auth/**", "/auth/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**",
            "/webjars/**", "/favicon.ico",
            "/actuator/health", "/actuator/health/**", "/actuator/info",
            "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // uri: 전체 경로(/api/auth/oauth), ctx: 컨텍스트(/api 또는 ""), path: 내부 경로(/auth/oauth)
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        String path = (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx))
                ? uri.substring(ctx.length())
                : uri;

        // 컨텍스트 유무에 상관없이 둘 다 매칭 시도
        return WHITELIST.stream().anyMatch(p ->
                matcher.match(p, path) || matcher.match(p, uri)
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws IOException, ServletException {

        String authHeader = req.getHeader("Authorization");

        // 토큰이 아예 없으면 인증 시도하지 않고 통과 (permitAll 경로는 shouldNotFilter로 이미 스킵됨)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        try {
            String token = jwtTokenProvider.resolveToken(req);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                var authentication = jwtTokenProvider.getAuthentication(token);
                if (authentication instanceof UsernamePasswordAuthenticationToken authenticationToken) {
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                chain.doFilter(req, res);
                return;
            }

            // 토큰이 있으나 유효하지 않은 경우: 401 JSON
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Invalid or expired token\"}");
        } catch (Exception ex) {
            // 예외 시에도 401 JSON으로 응답 (로그는 필요 시 추가)
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Token processing failed\"}");
        }
    }
}
