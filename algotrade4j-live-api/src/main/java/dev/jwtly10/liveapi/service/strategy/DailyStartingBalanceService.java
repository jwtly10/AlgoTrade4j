package dev.jwtly10.liveapi.service.strategy;

import dev.jwtly10.liveapi.model.DailyStartingBalance;
import dev.jwtly10.liveapi.model.LiveStrategy;
import dev.jwtly10.liveapi.repository.DailyStartingBalanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DailyStartingBalanceService {
    private final DailyStartingBalanceRepository dailyStartingBalanceRepository;

    public DailyStartingBalanceService(DailyStartingBalanceRepository dailyStartingBalanceRepository) {
        this.dailyStartingBalanceRepository = dailyStartingBalanceRepository;
    }

    public void setDailyStartBalance(LiveStrategy strategy, double balance, double equity, ZonedDateTime date) {
        DailyStartingBalance data = new DailyStartingBalance();
        data.setLiveStrategy(strategy);
        data.setBalance(BigDecimal.valueOf(balance));
        data.setEquity(BigDecimal.valueOf(equity));
        data.setDate(date);
    }

    public Optional<Double> getDailyStartingBalance(LiveStrategy strategy, ZonedDateTime now) {
        List<DailyStartingBalance> dailyStartingBalances = dailyStartingBalanceRepository.findAllByLiveStrategyId(strategy.getId());

        // For each one, check if the date is the same as the current date
        for (DailyStartingBalance dailyStartingBalance : dailyStartingBalances) {
            if (dailyStartingBalance.getDate().equals(now)) {
                return Optional.of(dailyStartingBalance.getBalance().doubleValue());
            }
        }

        return Optional.empty();
    }
}