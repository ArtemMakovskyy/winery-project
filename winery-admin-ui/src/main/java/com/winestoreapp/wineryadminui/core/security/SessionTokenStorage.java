package com.winestoreapp.wineryadminui.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.micrometer.observation.annotation.Observed;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SessionTokenStorage {
    private static final String TOKEN = "TOKEN";
    private static final String ROLES = "ROLES";

    @Observed(name = "session.storage.save")
    public void save(HttpSession session, String token) {
        session.setAttribute(TOKEN, token);
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            List<String> roles = decodedJWT.getClaim("roles").asList(String.class);
            if (roles != null) {
                session.setAttribute(ROLES, roles);
                log.debug("Roles saved to session.");
            }
        } catch (Exception e) {
            log.error("Failed to extract roles from JWT: {}", e.getMessage());
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
        log.info("Session {} invalidated", session.getId());
        session.invalidate();
    }
}