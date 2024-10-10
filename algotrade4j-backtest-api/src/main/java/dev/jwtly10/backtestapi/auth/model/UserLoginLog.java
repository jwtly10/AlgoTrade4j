package dev.jwtly10.backtestapi.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.jwtly10.shared.auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_login_log_tb")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnore
    private User user;
}