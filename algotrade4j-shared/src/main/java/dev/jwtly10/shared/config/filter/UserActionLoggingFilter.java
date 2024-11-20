package dev.jwtly10.shared.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class UserActionLoggingFilter extends OncePerRequestFilter {

    // Some routes we don't care about logging the action
    private final List<String> excludedPaths = Arrays.asList(
            "/api/v1/auth/verify",
            "/api/v1/monitor",
            "/health",
            // Endpoints used for polling
            "/api/v1/live/strategies",
            "/api/v1/accounts"

    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        filterChain.doFilter(request, response);

        // Log after the request has been processed, so we can check the status code (to gracefully handle 404s)
        if (!excludedPaths.contains(path) && response.getStatus() != HttpServletResponse.SC_NOT_FOUND) {
            String username = "anonymous";
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (principal instanceof UserDetails) {
                    username = ((UserDetails) principal).getUsername();
                } else {
                    username = principal.toString();
                }
            }

            String method = request.getMethod();

            log.info("User '{}' performed '{}' request on '{}'", username, method, path);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return excludedPaths.contains(path);
    }
}