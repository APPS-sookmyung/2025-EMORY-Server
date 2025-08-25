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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final List<String> WHITELIST = List.of(
            "/",
            "/ping",
            "/api/auth",
            "/v3/api-docs",
            "/swagger",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars",
            "/favicon.ico",
            "/actuator"
    );

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::contains);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isWhitelisted(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = jwtTokenProvider.resolveToken(request);
            System.out.println("추출된 토큰: " + token);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                var authentication = jwtTokenProvider.getAuthentication(token);

                if (authentication != null) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            (UsernamePasswordAuthenticationToken) authentication;

                    System.out.println("인증 성공: " + authenticationToken.getPrincipal());
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } else {
                    System.out.println("authentication == null");
                }
            } else {
                System.out.println("토큰 없음 or 유효하지 않음");
            }
        } catch (Exception e) {
            System.out.println("필터 처리 중 예외 발생: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

}
