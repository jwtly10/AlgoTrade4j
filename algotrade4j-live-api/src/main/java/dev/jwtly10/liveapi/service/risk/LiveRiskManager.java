package dev.jwtly10.liveapi.service.risk;

import dev.jwtly10.core.risk.DailyEquity;
import dev.jwtly10.core.risk.RiskManagementService;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public record LiveRiskManager(RiskManagementServiceClient client, String brokerAccountId) implements RiskManagementService {
    @Override
    public Optional<DailyEquity> getCurrentDayStartingEquity(){
        try {
            return client.getDailyEquity(brokerAccountId);
        } catch (Exception e) {
            log.error("Failed to fetch daily equity from external service: ", e);
            return Optional.empty();
        }
    }

    @Override
    public void setTimezone(String timezone) {
        // Not supported in live trading
    }
}
