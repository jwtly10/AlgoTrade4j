package dev.jwtly10.liveservice.service.biz;

import dev.jwtly10.common.exception.ApiException;
import dev.jwtly10.common.exception.ErrorType;
import dev.jwtly10.liveservice.model.BrokerAccount;
import dev.jwtly10.liveservice.model.LiveStrategy;
import dev.jwtly10.liveservice.model.Stats;
import dev.jwtly10.liveservice.repository.LiveStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LiveStrategyService {
    private final LiveStrategyRepository liveStrategyRepository;
    private final BrokerAccountService brokerAccountService;

    public LiveStrategyService(LiveStrategyRepository liveStrategyRepository, BrokerAccountService brokerAccountService) {
        this.liveStrategyRepository = liveStrategyRepository;
        this.brokerAccountService = brokerAccountService;
    }

    public List<LiveStrategy> getNonHiddenLiveStrategies() {
        log.info("Fetching all non-hidden live strategies");
        return liveStrategyRepository.findLiveStrategiesByHiddenIsFalse();
    }

    public LiveStrategy updateStrategyStats(Long liveStrategyId, Stats stats) {
        log.info("Updating live strategy stats for strategy ID: {}", liveStrategyId);

        // Validate that the LiveStrategy exists
        LiveStrategy liveStrategy = liveStrategyRepository.findById(liveStrategyId)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        liveStrategy.setStats(stats);
        return liveStrategyRepository.save(liveStrategy);
    }

    public LiveStrategy createLiveStrategy(LiveStrategy strategy) {
        log.info("Creating live strategy: {}", strategy.getStrategyName());
        // Validate live strategy configuration

        // Validate live strategy name must be unique

        // Validate broker config
        BrokerAccount brokerAccount = brokerAccountService.getBrokerAccount(strategy.getBrokerAccount().getAccountId());
        strategy.setBrokerAccount(brokerAccount);

        // Save the strategy to the database
        strategy.setActive(false); // Will force the user to activate the strategy specifically, when they want to run it
        return liveStrategyRepository.save(strategy);
    }

    public LiveStrategy toggleStrategy(Long id) {
        log.info("Activating live strategy: {}", id);

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        // Toggle the active strategy
        liveStrategy.setActive(!liveStrategy.isActive());
        return liveStrategyRepository.save(liveStrategy);
    }

    public void deleteStrategy(Long id) {
        log.info("Deleting live strategy: {}", id);
        // We dont actually delete the strategy, we just deactivate it and set hidden

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        liveStrategy.setActive(false);
        liveStrategy.setHidden(true);

        liveStrategyRepository.save(liveStrategy);
    }
}