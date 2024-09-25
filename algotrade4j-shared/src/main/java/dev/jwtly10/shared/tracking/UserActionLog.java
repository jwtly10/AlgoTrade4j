package dev.jwtly10.shared.tracking;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Table(name = "user_action_log_tb")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserAction action;

    @Column(columnDefinition = "json", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metaData;

    @Column(nullable = false)
    private ZonedDateTime timestamp;
}