package dev.jwtly10.liveservice.service;

import dev.jwtly10.common.exception.ApiException;
import dev.jwtly10.common.exception.ErrorType;
import dev.jwtly10.liveservice.model.LiveStrategy;
import dev.jwtly10.liveservice.model.LiveStrategyConfig;
import dev.jwtly10.liveservice.repository.LiveStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LiveStrategyService {
    private final LiveStrategyRepository liveStrategyRepository;

    public LiveStrategyService(LiveStrategyRepository liveStrategyRepository) {
        this.liveStrategyRepository = liveStrategyRepository;
    }

    public LiveStrategy createLiveStrategy(LiveStrategyConfig config, String strategyName) {
        log.info("Creating live strategy: {}", strategyName);
        config.validate();

        // Save the strategy to the database
        LiveStrategy liveStrategy = new LiveStrategy();
        liveStrategy.setStrategyName(strategyName);
        liveStrategy.setConfig(config);
        liveStrategy.setActive(false); // Will force the user to activate the strategy specifically

        return liveStrategyRepository.save(liveStrategy);
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