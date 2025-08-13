package emory.emoryserver.global.config.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    // 토큰 유효 시간: 1시간
    private final long tokenValidTime = 1000L * 60 * 60;

    // Bean 생성 후 시크릿 키 초기화
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // JWT 생성 - 이메일과 권한 리스트 포함
    public String createToken(String email, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(email); // sub = email
        claims.put("roles", roles); // 사용자 역할 추가

        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT로부터 인증 정보 추출
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject(); // sub에서 이메일 추출

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");

        List<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // principal = email (String)
        return new UsernamePasswordAuthenticationToken(email, "", authorities);
    }

    // JWT에서 이메일 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Request 헤더에서 Authorization: Bearer <token> 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }
        return null;
    }
}
