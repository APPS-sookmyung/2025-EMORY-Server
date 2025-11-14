package emory.emoryserver.global.config.auth;

<<<<<<< HEAD
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
=======
import io.jsonwebtoken.*;
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
<<<<<<< HEAD
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
=======
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
<<<<<<< HEAD
    private String secretKeyRaw;

    @Value("${jwt.expiration:3600000}")
    private long tokenValidTime;

    private Key key;

    @PostConstruct
    protected void init() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretKeyRaw);
        } catch (IllegalArgumentException e) {
            keyBytes = secretKeyRaw.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 32 bytes (256 bits). Current: " + keyBytes.length + " bytes");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // JWT 생성 - 이메일(subject)과 역할 리스트 포함
    public String createToken(String email, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(email); // sub = email
        claims.put("roles", roles);

=======
    private String secretKey;

    private Key key;

    private final UserDetailsService userDetailsService;

    private final long tokenValidTime = 1000L * 60 * 60;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(String username) {
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidTime);

        return Jwts.builder()
<<<<<<< HEAD
                .setClaims(claims)
=======
                .setSubject(username)
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

<<<<<<< HEAD
    // JWT로부터 Authentication 추출
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();

        Object rolesObj = claims.get("roles");
        List<String> roles;
        if (rolesObj instanceof List<?> list) {
            roles = list.stream().map(String::valueOf).collect(Collectors.toList());
        } else {
            roles = Collections.emptyList();
        }

        List<GrantedAuthority> authorities = roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(email, "", authorities);
    }

    // JWT에서 이메일(subject) 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {

=======
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        var userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
            return false;
        }
    }

<<<<<<< HEAD
    // Request 헤더에서 Bearer <token> 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }
        return null;
    }
=======
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

>>>>>>> 50a6162384f5c48587bb8bb05045ed41f702a54e
}
