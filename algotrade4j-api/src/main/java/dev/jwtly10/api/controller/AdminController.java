package dev.jwtly10.api.controller;

import dev.jwtly10.api.auth.model.LoginResponse;
import dev.jwtly10.api.auth.model.Role;
import dev.jwtly10.api.auth.model.SignupRequest;
import dev.jwtly10.api.auth.model.User;
import dev.jwtly10.api.auth.model.dto.UserDTO;
import dev.jwtly10.api.auth.service.UserService;
import dev.jwtly10.api.config.ratelimit.RateLimit;
import dev.jwtly10.common.exception.ApiException;
import dev.jwtly10.common.exception.ErrorType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @RateLimit(limit = 10)
    public ResponseEntity<?> createUser(@RequestBody SignupRequest user) {
        try {
            User newUser = userService.createUser(user.getUsername(), user.getEmail(), user.getPassword(), user.getFirstName(), user.getLastName());
            return ResponseEntity.ok(new LoginResponse(
                    newUser.getId(),
                    newUser.getUsername(),
                    newUser.getFirstName(),
                    newUser.getEmail(),
                    newUser.getRole().name()));
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), ErrorType.BAD_REQUEST);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsersDTO();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), ErrorType.INTERNAL_ERROR);
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @RequestBody User updatedUser) {
        try {
            User user = userService.updateUser(userId, updatedUser);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), ErrorType.BAD_REQUEST);
        }
    }

    @PostMapping("/users/{userId}/change-password")
    public ResponseEntity<?> changeUserPassword(@PathVariable("userId") Long userId, @RequestBody Map<String, String> passwordMap) {
        String newPassword = passwordMap.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            throw new ApiException("New password is required", ErrorType.BAD_REQUEST);
        }
        try {
            userService.changeUserPassword(userId, newPassword);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), ErrorType.BAD_REQUEST);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), ErrorType.INTERNAL_ERROR);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<List<String>> getRoles() {
        List<String> roles = Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }
}