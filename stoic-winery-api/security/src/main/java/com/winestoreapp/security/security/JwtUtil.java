package com.winestoreapp.security.security;

import com.winestoreapp.common.observability.ObservationNames;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.observation.annotation.Observed;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {
    private final TokenBlacklistService blacklistService;
    private final Key secret;

    @Value("${jwt.expiration:3600000}")
    private long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secretString, TokenBlacklistService blacklistService) {
        this.secret = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        this.blacklistService = blacklistService;
    }

    public void addToInvalidTokens(String token) {
        long ttlSeconds = getRemainingTtl(token);
        blacklistService.blacklist(token, ttlSeconds);
        log.debug("Token added to blacklist with TTL: {}s", ttlSeconds);
    }

    /**
     * Calculates remaining TTL for a token based on its expiration.
     *
     * @param token the JWT token
     * @return remaining time to live in seconds
     */
    private long getRemainingTtl(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingMs / 1000);
        } catch (Exception e) {
            log.warn("Failed to parse token for TTL calculation, using default expiration");
            return expiration / 1000;
        }
    }

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secret)
                .compact();
    }

    @Observed(name = ObservationNames.AUTH_VALIDATE_TOKEN)
    public Claims parseToken(String token) {
        if (blacklistService.isBlacklisted(token)) {
            log.warn("Attempt to use blacklisted token");
            throw new JwtException("Token is blacklisted");
        }

        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsername(String token) {
        try {
            return parseToken(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
