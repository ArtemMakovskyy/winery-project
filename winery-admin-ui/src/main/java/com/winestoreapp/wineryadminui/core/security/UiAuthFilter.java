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

        if (isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isUiPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession session = request.getSession(false);

        if (isNotAuthenticated(session)) {
            handleUnauthorized(response, path);
            return;
        }

        List<String> roles = storage.getRoles(session);
        log.trace("Processing UI request: {} with roles: {}", path, roles);

        if (isUsersPath(path) && !hasAdminRole(roles)) {
            handleForbidden(response, path, roles, "Admins only");
            return;
        }

        if (isManagerOnlyPath(path) && !hasManagerRole(roles)) {
            handleForbidden(response, path, roles, "Managers only");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator")
                || path.startsWith("/login")
                || path.startsWith("/static")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images")
                || path.equals("/favicon.ico");
    }

    private boolean isUiPath(String path) {
        return path.startsWith("/ui");
    }

    private boolean isUsersPath(String path) {
        return path.startsWith("/ui/users");
    }

    private boolean isManagerOnlyPath(String path) {
        return path.startsWith("/ui/wines") || path.startsWith("/ui/orders");
    }

    private boolean isNotAuthenticated(HttpSession session) {
        return session == null || storage.get(session) == null;
    }

    private boolean hasAdminRole(List<String> roles) {
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    private boolean hasManagerRole(List<String> roles) {
        return roles != null && roles.contains("ROLE_MANAGER");
    }

    private void handleUnauthorized(HttpServletResponse response, String path) throws IOException {
        log.info("Unauthorized access attempt to {}. Redirecting to /login", path);
        response.sendRedirect("/login");
    }

    private void handleForbidden(HttpServletResponse response,
                                 String path,
                                 List<String> roles,
                                 String message) throws IOException {

        log.warn("Access denied to {}. User roles: {}", path, roles);
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Access Denied: " + message);
    }
}
