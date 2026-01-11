package com.winestoreapp.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
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

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secret)
                .compact();
    }

    public Claims parseToken(String token) {
        if (INVALID_TOKENS.containsKey(token)) {
            throw new JwtException("Token is blacklisted");
        }
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Scheduled(cron = EVERY_DAY_AT_MIDNIGHT)
    private void deleteAllExpiredTokensBySchedule() {
        LocalDateTime threshold = LocalDateTime.now().minus(expiration, ChronoUnit.MILLIS);
        INVALID_TOKENS.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
    }
}