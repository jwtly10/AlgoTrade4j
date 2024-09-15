package dev.jwtly10.api.auth.service;

import dev.jwtly10.shared.auth.model.Role;
import dev.jwtly10.shared.auth.model.User;
import dev.jwtly10.shared.auth.model.dto.UserDTO;
import dev.jwtly10.shared.auth.repository.UserRepository;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDTO> getAllUsersDTO() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(user.getId());
                    userDTO.setUsername(user.getUsername());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setFirstName(user.getFirstName());
                    userDTO.setLastName(user.getLastName());
                    userDTO.setRole(user.getRole().name());
                    userDTO.setCreatedAt(user.getCreatedAt());
                    userDTO.setUpdatedAt(user.getUpdatedAt());
                    return userDTO;
                })
                .sorted(Comparator.comparing(UserDTO::getId))
                .toList();
    }

    public User updateUser(Long userId, User updatedUser) {
        // Check username exists
        User userWithUsername = userRepository.findByUsername(updatedUser.getUsername()).orElse(null);
        if (userWithUsername != null && !userWithUsername.getId().equals(userId)) {
            throw new ApiException("Username is already taken!", ErrorType.BAD_REQUEST);
        }

        // Check email exists
        User userWithEmail = userRepository.findByEmail(updatedUser.getEmail()).orElse(null);
        if (userWithEmail != null && !userWithEmail.getId().equals(userId)) {
            throw new ApiException("Email is already taken!", ErrorType.BAD_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", ErrorType.NOT_FOUND));
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setRole(updatedUser.getRole());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", ErrorType.NOT_FOUND));

        userRepository.deleteById(userId);
    }

    public User createUser(String username, String email, String password, String firstName, String lastName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.BASIC_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Check username exists
        User userWithUsername = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (userWithUsername != null) {
            throw new ApiException("Username is already taken!", ErrorType.BAD_REQUEST);
        }

        // Check email exists
        User userWithEmail = userRepository.findByEmail(user.getEmail()).orElse(null);
        if (userWithEmail != null) {
            throw new ApiException("Email is already taken!", ErrorType.BAD_REQUEST);
        }

        return userRepository.save(user);
    }

    public User changeUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", ErrorType.NOT_FOUND));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public boolean checkPassword(User user, String providedPassword) {
        return passwordEncoder.matches(providedPassword, user.getPassword());
    }
}