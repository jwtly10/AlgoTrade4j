package dev.jwtly10.core.risk;

import java.util.Optional;

/**
 * The RiskManagementService is responsible for managing data for calculations for risk management.
 * <p>
 *     It's an independent service that tracks daily equity <a href="https://github.com/jwtly10/at4j-risk-manager">at4j-risk-manager</a>
 * </p>
 * <p>
 *     Which can then be used for managing daily drawdowns, account loss limits, safety buffers, and profit targets.
 * </p>
 * <p>
 *     If you want to use this service in your strategy when backtesting, you must configure the timezone to match the timezone of the prop firm you are backtesting for.
 * </p>
 * <p>
 *     This can be done via parameters, or any method you choose. In live trading this timezone is hardcoded to the Prop Firm's timezone, depending on broker configuration
 * </p>
 *
 */
public interface RiskManagementService {
    /**
     * Get the current day starting equity for the account.
     * @return the daily equity if exists, otherwise empty.
     */
    Optional<DailyEquity> getCurrentDayStartingEquity();

    /**
     * Used to support backtesting and dynamically updating the daily equity based on the current tick.
     * <p>
     *     Ignored during live trading. This value is predetermined by the broker.
     * </p>
     * @param timezone the timezone to use for the daily equity calculations (to backtest prop firm rules)
     *
     */
    void setTimezone(String timezone);
}
