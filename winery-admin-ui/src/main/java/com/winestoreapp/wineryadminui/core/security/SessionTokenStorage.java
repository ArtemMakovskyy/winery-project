package com.winestoreapp.wineryadminui.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionTokenStorage {
    private static final String TOKEN = "TOKEN";
    private static final String ROLES = "ROLES";

    public void save(HttpSession session, String token) {
        session.setAttribute(TOKEN, token);
        log.info("Session started for ID: {}. Token saved.", session.getId());
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
            if (roles != null) {
                session.setAttribute(ROLES, roles);
                log.debug("Extracted roles from JWT: {}", roles);
            }
        } catch (Exception e) {
            log.error("CRITICAL: Failed to decode roles from token for session {}: {}", session.getId(), e.getMessage());
        }
    }

    public String get(HttpSession session) {
        return (String) session.getAttribute(TOKEN);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(HttpSession session) {
        return (List<String>) session.getAttribute(ROLES);
    }

    public void clear(HttpSession session) {
        log.info("Invalidating session: {}", session.getId());
        session.invalidate();
    }
}