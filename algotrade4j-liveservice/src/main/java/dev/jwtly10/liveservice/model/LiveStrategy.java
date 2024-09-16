package dev.jwtly10.liveservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "live_strategies_tb")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveStrategy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_name", nullable = false)
    private String strategyName;

    @OneToOne
    @JoinColumn(name = "broker_account_id", nullable = false)
    private BrokerAccount brokerAccount;

    @Column(nullable = false, columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private LiveStrategyConfig config;

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private Stats stats;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    @Column(name = "last_error_msg")
    private String lastErrorMsg;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}