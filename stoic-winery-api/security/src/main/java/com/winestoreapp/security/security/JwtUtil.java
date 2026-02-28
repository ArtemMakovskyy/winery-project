package com.winestoreapp.security.security;

import com.winestoreapp.common.observability.ObservationNames;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micrometer.observation.annotation.Observed;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {
    private static final Map<String, LocalDateTime> INVALID_TOKENS = new ConcurrentHashMap<>();
    private static final String EVERY_DAY_AT_MIDNIGHT = "0 0 0 * * *";
    private final Key secret;

    @Value("${jwt.expiration:3600000}")
    private long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secretString) {
        this.secret = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    public void addToInvalidTokens(String token) {
        INVALID_TOKENS.put(token, LocalDateTime.now());
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

        if (INVALID_TOKENS.containsKey(token)) {
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

    @Scheduled(cron = EVERY_DAY_AT_MIDNIGHT)
    public void deleteAllExpiredTokensBySchedule() {
        log.info("Starting scheduled cleanup of invalidated tokens");
        int initialSize = INVALID_TOKENS.size();
        LocalDateTime threshold = LocalDateTime.now().minus(expiration, ChronoUnit.MILLIS);
        INVALID_TOKENS.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
        log.info("Cleanup finished. Removed {} tokens", initialSize - INVALID_TOKENS.size());
    }
}
