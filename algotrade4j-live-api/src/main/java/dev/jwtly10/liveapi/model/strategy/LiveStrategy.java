package dev.jwtly10.liveapi.model.strategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.utils.StrategyReflectionUtils;
import dev.jwtly10.liveapi.model.Stats;
import dev.jwtly10.liveapi.model.broker.BrokerAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

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

    /**
     * Validate the live strategy configuration against the real strategy class.
     * TODO: Do we need more validation
     * For now we just validate run parameters, as these are the configuration items that are class specific,
     * So will fail if we edit any of the used classes.
     *
     * @throws Exception if the configuration is invalid
     */
    public void validateConfigAgainstStrategyClass() throws Exception {
        Map<String, String> runParams = this.config.getRunParams().stream().collect(
                Collectors.toMap(LiveStrategyConfig.RunParameter::getName, LiveStrategyConfig.RunParameter::getValue));

        Strategy stratInstance = StrategyReflectionUtils.getStrategyFromClassName(this.config.getStrategyClass(), null);

        ParameterHandler.validateRunParameters(stratInstance, runParams);
    }
}