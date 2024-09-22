package dev.jwtly10.liveservice.service.strategy;

import dev.jwtly10.liveservice.model.BrokerAccount;
import dev.jwtly10.liveservice.model.LiveStrategy;
import dev.jwtly10.liveservice.model.Stats;
import dev.jwtly10.liveservice.repository.BrokerAccountRepository;
import dev.jwtly10.liveservice.repository.LiveStrategyRepository;
import dev.jwtly10.shared.auth.utils.SecurityUtils;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import dev.jwtly10.shared.tracking.TrackingService;
import dev.jwtly10.shared.tracking.UserAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LiveStrategyService {
    private final LiveStrategyRepository liveStrategyRepository;
    private final BrokerAccountRepository brokerAccountRepository;

    private final TrackingService trackingService;

    public LiveStrategyService(LiveStrategyRepository liveStrategyRepository, BrokerAccountRepository brokerAccountRepository, TrackingService trackingService) {
        this.liveStrategyRepository = liveStrategyRepository;
        this.brokerAccountRepository = brokerAccountRepository;
        this.trackingService = trackingService;
    }

    public List<LiveStrategy> getNonHiddenLiveStrategies() {
        log.info("Fetching all non-hidden live strategies");
        return liveStrategyRepository.findLiveStrategiesByHiddenIsFalse();
    }

    public List<LiveStrategy> getActiveLiveStrategies() {
        log.info("Fetching all active live strategies");
        return liveStrategyRepository.findLiveStrategiesByHiddenIsFalseAndActiveIsTrue();
    }

    public void setErrorMessage(LiveStrategy liveStrategy, String errorMessage) {
        log.info("Setting error message for live strategy: {}", liveStrategy.getId());

        liveStrategy.setLastErrorMsg(errorMessage);
        liveStrategyRepository.save(liveStrategy);
    }

    public LiveStrategy updateStrategyStats(Long liveStrategyId, Stats stats) {
        log.info("Updating live strategy stats for strategy ID: {}", liveStrategyId);

        // Validate that the LiveStrategy exists
        LiveStrategy liveStrategy = liveStrategyRepository.findById(liveStrategyId)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        liveStrategy.setStats(stats);
        return liveStrategyRepository.save(liveStrategy);
    }

    public LiveStrategy deactivateStrategy(String strategyName) {
        log.info("Force deactivating live strategy: {}", strategyName);

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findByStrategyName(strategyName)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        liveStrategy.setActive(false);
        return liveStrategyRepository.save(liveStrategy);
    }

    public LiveStrategy createLiveStrategy(LiveStrategy strategy) {
        log.info("Creating live strategy: {}", strategy.getStrategyName());

        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.LIVE_STRATEGY_CREATE,
                Map.of(
                        "strategyName", strategy.getStrategyName(),
                        "config", strategy.getConfig()
                )
        );

        // Validate live strategy configuration
        LiveStrategy existingStrategy = liveStrategyRepository.findByStrategyName(strategy.getStrategyName()).orElse(null);
        if (existingStrategy != null) {
            throw new ApiException("Strategy with the same name already exists", ErrorType.BAD_REQUEST);
        }

        try {
            strategy.getConfig().validate();
        } catch (Exception e) {
            log.error("Invalid live strategy configuration", e);
            throw new ApiException("Invalid live strategy configuration: " + e.getMessage(), ErrorType.BAD_REQUEST);
        }

        // Validate broker config
        BrokerAccount brokerAccount = brokerAccountRepository.findById(strategy.getBrokerAccount().getId())
                .orElseThrow(() -> new ApiException("Broker account not found", ErrorType.NOT_FOUND));


        // Validate broker not in use
        List<LiveStrategy> liveStrategies = liveStrategyRepository.findLiveStrategiesByBrokerAccountAndHiddenIsFalse(brokerAccount);
        if (!liveStrategies.isEmpty()) {
            throw new ApiException("Broker account is already in use", ErrorType.BAD_REQUEST);
        }

        strategy.setBrokerAccount(brokerAccount);

        // Save the strategy to the database
        strategy.setActive(false); // Will force the user to activate the strategy specifically, when they want to run it
        return liveStrategyRepository.save(strategy);
    }

    public LiveStrategy toggleStrategy(Long id) {
        log.info("Toggling live strategy: {}", id);

        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.LIVE_STRATEGY_TOGGLE,
                Map.of("strategyId", id)
        );

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        // Toggle the active strategy
        liveStrategy.setActive(!liveStrategy.isActive());
        return liveStrategyRepository.save(liveStrategy);
    }

    public void deleteStrategy(Long id) {
        // We dont actually delete the strategy, we just deactivate it and set hidden
        log.info("Deleting live strategy: {}", id);

        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.LIVE_STRATEGY_DELETE,
                Map.of("strategyId", id)
        );

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        liveStrategy.setActive(false);
        liveStrategy.setHidden(true);

        liveStrategyRepository.save(liveStrategy);
    }

    public LiveStrategy updateLiveStrategy(Long id, LiveStrategy strategySetup) {
        log.info("Updating live strategy: {}", strategySetup.getStrategyName());

        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.LIVE_STRATEGY_EDIT,
                Map.of(
                        "strategyId", id,
                        "strategyName", strategySetup.getStrategyName(),
                        "config", strategySetup.getConfig()
                )
        );

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        try {
            strategySetup.getConfig().validate();
        } catch (Exception e) {
            log.error("Invalid live strategy configuration", e);
            throw new ApiException("Invalid live strategy configuration: " + e.getMessage(), ErrorType.BAD_REQUEST);
        }

        // Update the possible values that we allow for updating
        liveStrategy.setStrategyName(strategySetup.getStrategyName());
        liveStrategy.setConfig(strategySetup.getConfig());
        liveStrategy.setActive(false); // If we make updated, we should deactivate the strategy

        // Save the strategy to the database
        return liveStrategyRepository.save(strategySetup);
    }

    public List<LiveStrategy> findByBrokerAccount(BrokerAccount brokerAccount) {
        return liveStrategyRepository.findLiveStrategiesByBrokerAccountAndHiddenIsFalse(brokerAccount);
    }
}