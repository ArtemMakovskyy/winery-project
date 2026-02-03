package com.winestoreapp.wineryadminui.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class UiAuthFilter extends OncePerRequestFilter {

    private final SessionTokenStorage storage;

    public UiAuthFilter(SessionTokenStorage storage) {
        this.storage = storage;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/ui")) {
            HttpSession session = request.getSession(false);

            if (session == null || storage.get(session) == null) {
                log.info("Unauthorized access attempt to {}. Redirecting to /login", path);
                response.sendRedirect("/login");
                return;
            }

            List<String> roles = storage.getRoles(session);
            log.trace("Processing UI request: {} with roles: {}", path, roles);

            if (path.startsWith("/ui/users") && (roles == null || !roles.contains("ROLE_ADMIN"))) {
                log.error("SECURITY ALERT: Access denied to /ui/users. User roles: {}", roles);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admins only");
                return;
            }

            if ((path.startsWith("/ui/wines") || path.startsWith("/ui/orders"))
                    && (roles == null || !roles.contains("ROLE_MANAGER"))) {
                log.warn("Access denied to {} for roles: {}", path, roles);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Managers only");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}