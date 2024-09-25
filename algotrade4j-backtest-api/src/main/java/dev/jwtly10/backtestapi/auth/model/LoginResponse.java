package dev.jwtly10.backtestapi.auth.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {
    private Long id;
    private String username;
    private String firstName;
    private String email;
    private String role;

    public LoginResponse(Long id, String username, String firstName, String email, String role) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.email = email;
        this.role = role;
    }
}