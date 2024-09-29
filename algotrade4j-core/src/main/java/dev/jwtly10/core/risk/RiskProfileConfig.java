package dev.jwtly10.core.risk;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.time.ZoneId;

@Getter
@Builder
public class RiskProfileConfig {
    /**
     * Maximum daily drawdown as a flat amount
     */
    private final Double maxDailyLoss;
    /**
     * Maximum loss an account can take (equity)
     */
    private final Double accountLossLimit;
    /**
     * Buffer to add to the account loss limit to not hit the limit exactly
     */
    private final Double safetyBuffer;
    /**
     * Profit target as to when to stop trading
     */
    private final Double profitTarget;

    /**
     * The time zone of the broker, used to calculate the trading day
     */
    @Builder.Default
    private final ZoneId brokerTimeZone = ZoneId.of("UTC");

    /**
     * The time the trading day starts in the brokers time zone
     */
    @Builder.Default
    private final LocalTime tradingDayStart = LocalTime.of(0, 0);
}