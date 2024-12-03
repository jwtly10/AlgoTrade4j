package dev.jwtly10.core.risk;

import dev.jwtly10.core.model.Tick;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This risk manager is used for backtesting purposes only.
 * <p>
 *     It manually tracks the daily equity of the account and updates it based on the current tick.
 *     In live trading, the daily equity is predetermined by the broker, and set using the external service <a href="https://github.com/jwtly10/at4j-risk-manager">at4j-risk-manager</a>
 * </p>
 */
@Slf4j
public class BacktestRiskManager implements RiskManagementService {
    private static final LocalTime NEW_DAY_TIME = LocalTime.of(0, 0, 1);
    private final List<DailyEquity> dailyEquities = new ArrayList<>();
    private ZoneId timezone;
    private ZonedDateTime lastUpdatedTime;

    @Override
    public Optional<DailyEquity> getCurrentDayStartingEquity() {
        if (dailyEquities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(dailyEquities.getLast());
    }

    @Override
    public void setTimezone(String timezone) {
        this.timezone = ZoneId.of(timezone);
    }

    public void checkAndSetOnTick(Tick currentTick, Double currentEquity) {
        if (timezone == null) {
            throw new IllegalStateException("Timezone is not set");
        }

        // Convert UTC tick time to target timezone
        ZonedDateTime tickTimeInZone = currentTick.getDateTime().withZoneSameInstant(timezone);

        // Initialize lastUpdatedTime if null
        if (lastUpdatedTime == null) {
            lastUpdatedTime = tickTimeInZone;
            dailyEquities.add(new DailyEquity("backtestingAccount", currentEquity, tickTimeInZone));
            return;
        }

        // Check if a new day has started
        if (isNewDay(lastUpdatedTime, tickTimeInZone)) {
            dailyEquities.add(new DailyEquity("backtestingAccount", currentEquity, tickTimeInZone));
            lastUpdatedTime = tickTimeInZone;
        }
    }

    private boolean isNewDay(ZonedDateTime lastTime, ZonedDateTime currentTime) {
        // Get the next expected update time
        ZonedDateTime nextUpdateTime = lastTime.toLocalDate()
                .plusDays(1)
                .atTime(NEW_DAY_TIME)
                .atZone(timezone);

        // Check if current time has passed the next update time
        return currentTime.isAfter(nextUpdateTime) || currentTime.isEqual(nextUpdateTime);
    }
}