package dev.jwtly10.core.risk;

import lombok.Getter;

import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Preset risk profiles
 */
@Getter
public enum RiskProfile {
    // TODO: Properly implement the different Risk Profiles
    NONE(RiskProfileConfig.builder()
            .maxDailyLoss(null)
            .accountLossLimit(null)
            .safetyBuffer(null)
            .profitTarget(null)
            .brokerTimeZone(ZoneId.of("UTC"))
            .tradingDayStart(LocalTime.of(0, 0))
            .build()),

    FTMO(RiskProfileConfig.builder()
            .maxDailyLoss(100.0)
            .accountLossLimit(500.0)
            .safetyBuffer(50.0)
            .profitTarget(200.0)
            .brokerTimeZone(ZoneId.of("UTC"))
            .tradingDayStart(LocalTime.of(0, 0))
            .build()),

    MFF(RiskProfileConfig.builder()
            .maxDailyLoss(200.0)
            .accountLossLimit(1000.0)
            .safetyBuffer(100.0)
            .profitTarget(null)
            .profitTarget(2000.0)
            .brokerTimeZone(ZoneId.of("UTC"))
            .tradingDayStart(LocalTime.of(0, 0))
            .build()),

    INTEGRATION_TEST(RiskProfileConfig.builder()
            .maxDailyLoss(200.0)
            .accountLossLimit(1000.0)
            .safetyBuffer(100.0)
            .profitTarget(null)
            .profitTarget(2000.0)
            .brokerTimeZone(ZoneId.of("UTC"))
            .tradingDayStart(LocalTime.of(0, 0))
            .build());
    private final RiskProfileConfig config;

    RiskProfile(RiskProfileConfig config) {
        this.config = config;
    }
}