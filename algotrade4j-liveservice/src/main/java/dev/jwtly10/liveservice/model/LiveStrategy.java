package dev.jwtly10.liveservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * LiveStrategy
 */
@Entity
@Table(name = "live_strategy_tb")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiveStrategy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_name", nullable = false)
    private String strategyName;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(nullable = false, columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private LiveStrategyConfig config;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}