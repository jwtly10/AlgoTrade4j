package dev.jwtly10.core.risk;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Slf4j
public class RiskManager {
    private final RiskProfileConfig config;
    private final AccountManager accountManager;
    private final EventPublisher eventPublisher;
    private double dailyStartEquity;
    private ZonedDateTime currentTradingDay;

    private final String strategyId;

    public RiskManager(RiskProfileConfig config, AccountManager accountManager, EventPublisher eventPublisher, String strategyId, ZonedDateTime startTime) {
        this.config = config;
        this.accountManager = accountManager;
        this.eventPublisher = eventPublisher;
        this.currentTradingDay = calculateCurrentTradingDay(startTime.withZoneSameInstant(config.getBrokerTimeZone()));
        this.dailyStartEquity = accountManager.getEquity();
        this.strategyId = strategyId;
    }

    /**
     * Assess the risk of the current account state given the risk configuration
     *
     * @param currentTime the current time
     * @return the risk status
     */
    public RiskStatus assessRisk(ZonedDateTime currentTime) {
        checkNewDay(currentTime);
        String violationReason = getRiskViolationReason();
        return new RiskStatus(violationReason != null, violationReason);
    }

    /**
     * Check if the account can trade, given the risk configuration
     *
     * @return the risk status
     */
    public RiskStatus canTrade() {
        String violationReason = getRiskViolationReason();
        if (violationReason == null) {
            return new RiskStatus(false, null);
        } else {
            return new RiskStatus(true, violationReason);
        }
    }

    private void checkNewDay(ZonedDateTime currentTime) {
        ZonedDateTime newTradingDay = calculateCurrentTradingDay(currentTime);
        if (!newTradingDay.equals(currentTradingDay)) {
            currentTradingDay = newTradingDay;
            dailyStartEquity = accountManager.getEquity();
            log.info("New trading day started at {}. Daily start equity set to: {}", currentTime, dailyStartEquity);
        }
    }

    private String getRiskViolationReason() {
        double currentEquity = accountManager.getEquity();
        double initialBalance = accountManager.getInitialBalance();

        if (config.getMaxDailyLoss() != null && isDailyLossExceeded(currentEquity)) {
            return "Daily loss limit of " + config.getMaxDailyLoss() + " exceeded. Current daily loss: " + (dailyStartEquity - currentEquity);
        }

        if (config.getAccountLossLimit() != null && isAccountLossLimitApproached(currentEquity, initialBalance)) {
            double totalLoss = initialBalance - currentEquity;
            return "Account loss limit of " + config.getAccountLossLimit() + " approached. Current total loss: " + totalLoss;
        }

        if (config.getProfitTarget() != null && isProfitTargetReached(currentEquity, initialBalance)) {
            double currentProfit = currentEquity - initialBalance;
            return "Profit target of " + config.getProfitTarget() + " reached. Current profit: " + currentProfit;
        }

        return null; // No violation
    }

    private ZonedDateTime calculateCurrentTradingDay(ZonedDateTime currentTime) {
        ZonedDateTime tradingDayStart = currentTime.withZoneSameInstant(config.getBrokerTimeZone())
                .with(config.getTradingDayStart());

        if (currentTime.isBefore(tradingDayStart)) {
            tradingDayStart = tradingDayStart.minusDays(1);
        }

        return tradingDayStart;
    }

    private boolean isDailyLossExceeded(double currentEquity) {
        double dailyLoss = dailyStartEquity - currentEquity;
        return dailyLoss > config.getMaxDailyLoss();
    }

    private boolean isAccountLossLimitApproached(double currentEquity, double initialBalance) {
        double totalLoss = initialBalance - currentEquity;
        return totalLoss > (config.getAccountLossLimit() - config.getSafetyBuffer());
    }

    private boolean isProfitTargetReached(double currentEquity, double initialBalance) {
        double currentProfit = currentEquity - initialBalance;
        return currentProfit >= config.getProfitTarget();
    }
}