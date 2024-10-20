package dev.jwtly10.shared.auth.filter;

import dev.jwtly10.shared.auth.service.UserDetailsServiceImpl;
import dev.jwtly10.shared.auth.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            String username = jwtUtils.getUserNameFromJwtToken(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            String userAgent = request.getHeader("User-Agent");
            String path = request.getRequestURI();
            String method = request.getMethod();

            // Cloudflare headers
            String ipAddress = request.getHeader("CF-Connecting-IP");
            String country = request.getHeader("CF-IPCountry");
            String cfRay = request.getHeader("CF-RAY");

            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }

            // We can ignore the /api/v1/auth/verify path as it is used to verify the JWT token
            // And isn't a concern - it checks on all page loads, if someone just accesses /login, it will trigger and log, but it's not worth logging as malicious
            // We can ignore /health since it's a health check
            // We can ignore /api/v1/marketdata/** since it's a public endpoint, and uses API keys for authentication
            // Update 15 October, removed logging due to log spam
            // if (!Objects.equals(path, "/api/v1/auth/verify") && !Objects.equals(path, "/health") && !path.contains("/api/v1/marketdata/candles")) {
                // log.warn("Unauthenticated access attempt: Method: {}, Path: {}, IP: {}, Country: {}, CF-RAY: {}, User-Agent: {}",
                //         method, path, ipAddress, country, cfRay, userAgent);
            // }
        }

        filterChain.doFilter(request, response);
    }
}