package com.winestoreapp.wineryadminui.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@ConfigurationProperties(prefix = "api.filter.public")
public class UiAuthFilter extends OncePerRequestFilter {

    private List<String> paths;

    private SessionTokenStorage storage;

    @Autowired
    public void setStorage(SessionTokenStorage storage) {
        this.storage = storage;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isPublicPath(path) || !isUiPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!isAuthenticated(request)) {
            redirectToLogin(response, path);
            return;
        }
        if (isAdminPath(path) && !hasRole(getRoles(request), "ROLE_ADMIN")) {
            handleForbidden(response, path, getRoles(request), "Admin access required");
            return;
        }
        if (isManagerPath(path) && !hasRole(getRoles(request), "ROLE_MANAGER")) {
            handleForbidden(response, path, getRoles(request), "Manager access required");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isUiPath(String path) {
        return path.startsWith("/ui");
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && storage.get(session) != null;
    }

    private void redirectToLogin(HttpServletResponse response, String path) throws IOException {
        log.info("Unauthorized access attempt to {}. Redirecting to login.", path);
        response.sendRedirect("/login");
    }

    private boolean isAdminPath(String path) {
        return path.startsWith("/ui/users");
    }

    private boolean isManagerPath(String path) {
        return path.startsWith("/ui/wines") || path.startsWith("/ui/orders");
    }

    private List<String> getRoles(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return storage.getRoles(session);
    }

    private boolean isPublicPath(String path) {
        return paths != null && paths.stream().anyMatch(path::startsWith);
    }

    private boolean hasRole(List<String> roles, String requiredRole) {
        return roles != null && roles.contains(requiredRole);
    }

    private void handleForbidden(
            HttpServletResponse response,
            String path,
            List<String> roles,
            String msg
    ) throws IOException {
        log.warn("Access denied for path {}. Roles: {}", path, roles);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, msg);
    }
}
