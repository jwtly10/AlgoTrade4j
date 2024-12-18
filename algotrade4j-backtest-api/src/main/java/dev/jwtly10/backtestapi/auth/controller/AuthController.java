package dev.jwtly10.backtestapi.auth.controller;

import dev.jwtly10.backtestapi.auth.model.LoginRequest;
import dev.jwtly10.backtestapi.auth.model.LoginResponse;
import dev.jwtly10.backtestapi.auth.model.MessageResponse;
import dev.jwtly10.backtestapi.auth.model.SignupRequest;
import dev.jwtly10.backtestapi.auth.service.UserLoginLogService;
import dev.jwtly10.backtestapi.auth.service.UserService;
import dev.jwtly10.shared.auth.model.User;
import dev.jwtly10.shared.auth.model.UserDetailsImpl;
import dev.jwtly10.shared.auth.repository.UserRepository;
import dev.jwtly10.shared.auth.service.UserDetailsServiceImpl;
import dev.jwtly10.shared.auth.utils.JwtUtils;
import dev.jwtly10.shared.auth.utils.SecurityUtils;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import dev.jwtly10.shared.tracking.TrackingService;
import dev.jwtly10.shared.tracking.UserAction;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
    final AuthenticationManager authenticationManager;

    final UserRepository userRepository;

    final UserService userService;

    final PasswordEncoder encoder;

    final JwtUtils jwtUtils;

    final UserDetailsServiceImpl userDetailsService;

    final UserLoginLogService userLoginLogService;

    private final TrackingService trackingService;

    @Value("${app.signup.enabled:false}")
    private boolean signupEnabled;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, UserService userService, PasswordEncoder encoder, JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, UserLoginLogService userLoginLogService, TrackingService trackingService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.userLoginLogService = userLoginLogService;
        this.trackingService = trackingService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String role = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList()).get(0);

            String domain = request.getHeader("Domain");

            // If the domain header is not set, fall back to the req server name
            if (domain == null || domain.isEmpty()) {
                domain = request.getServerName();
            }
            String topLevelDomain = extractTopLevelDomain(domain);


            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setMaxAge(24 * 60 * 60);
            jwtCookie.setPath("/");
            jwtCookie.setDomain(topLevelDomain); // Set the domain to the top-level domain

            // Add the cookie to the response
            response.addCookie(jwtCookie);

            // Log the user login IP
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            // If successful login, log the user login to db
            userLoginLogService.logUserLogin(userDetails.getId(), ipAddress, userAgent);

            // Track the user login
            trackingService.track(userDetails.getId(), UserAction.LOGIN, null);

            // If we have found the user details. This should always be found
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            // Return user details without the JWT
            return ResponseEntity.ok(new LoginResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    userDetails.getEmail(),
                    role));
        } catch (BadCredentialsException e) {
            log.warn("Login attempt failed for user: {}", loginRequest.getUsername());
            throw new ApiException("Invalid username or password", ErrorType.UNAUTHORIZED);
        } catch (Exception e) {
            // TODO: Do we need to handle anything like this?
            log.error("Unexpected Error - Authentication failed: {}", e.getMessage());
            throw new ApiException("Authentication failed: " + e.getMessage(), ErrorType.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");

        response.addCookie(jwtCookie);

        trackingService.track(SecurityUtils.getCurrentUserId(), UserAction.LOGOUT, null);

        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("jwt")) {
                        String token = cookie.getValue();
                        if (jwtUtils.validateJwtToken(token)) {
                            String username = jwtUtils.getUserNameFromJwtToken(token);
                            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                            String role = userDetails.getAuthorities().stream()
                                    .map(item -> item.getAuthority())
                                    .collect(Collectors.toList()).get(0);

                            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

                            return ResponseEntity.ok(new LoginResponse(
                                    userDetails.getId(),
                                    userDetails.getUsername(),
                                    user.getFirstName(),
                                    user.getLastName(),
                                    userDetails.getEmail(),
                                    role
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ApiException("Error verifying token: " + e.getMessage(), ErrorType.UNAUTHORIZED);
        }
        throw new ApiException("User not authenticated", ErrorType.UNAUTHORIZED);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (!signupEnabled) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Error: Sign-up is currently disabled."));
        }

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        userService.createUser(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword(),
                signUpRequest.getFirstName(),
                signUpRequest.getLastName());

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("CF-Connecting-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // If it's an IPv6 address, keep only the first part
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    private String extractTopLevelDomain(String domain) {
        String[] parts = domain.split("\\.");
        if (parts.length > 1) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        }
        return domain;
    }
}