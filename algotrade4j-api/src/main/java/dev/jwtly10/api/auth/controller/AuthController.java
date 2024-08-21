package dev.jwtly10.api.auth.controller;

import dev.jwtly10.api.auth.config.JwtUtils;
import dev.jwtly10.api.auth.model.*;
import dev.jwtly10.api.auth.repository.UserRepository;
import dev.jwtly10.api.auth.service.UserDetailsServiceImpl;
import dev.jwtly10.api.auth.service.UserLoginLogService;
import dev.jwtly10.api.auth.service.UserService;
import dev.jwtly10.api.exception.ApiException;
import dev.jwtly10.api.exception.ErrorType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    final AuthenticationManager authenticationManager;

    final UserRepository userRepository;

    final UserService userService;

    final PasswordEncoder encoder;

    final JwtUtils jwtUtils;

    final UserDetailsServiceImpl userDetailsService;

    final UserLoginLogService userLoginLogService;

    @Value("${app.signup.enabled:false}")
    private boolean signupEnabled;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, UserService userService, PasswordEncoder encoder, JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService, UserLoginLogService userLoginLogService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.userLoginLogService = userLoginLogService;
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

            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setMaxAge(24 * 60 * 60);
            jwtCookie.setPath("/");

            // Add the cookie to the response
            response.addCookie(jwtCookie);

            // Log the user login IP
            String ipAddress = request.getRemoteAddr();
            userLoginLogService.logUserLogin(userDetails.getId(), ipAddress);

            // Return user details without the JWT
            return ResponseEntity.ok(new LoginResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    role));
        } catch (BadCredentialsException e) {
            throw new ApiException("Invalid username or password", ErrorType.UNAUTHORIZED);
        } catch (AuthenticationException e) {
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

                            return ResponseEntity.ok(new LoginResponse(
                                    userDetails.getId(),
                                    userDetails.getUsername(),
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
}