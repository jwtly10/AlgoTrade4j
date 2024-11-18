package dev.jwtly10.liveapi.service.strategy;

import dev.jwtly10.core.strategy.ParameterHandler;
import dev.jwtly10.core.strategy.Strategy;
import dev.jwtly10.core.utils.StrategyReflectionUtils;
import dev.jwtly10.liveapi.model.Stats;
import dev.jwtly10.liveapi.model.broker.BrokerAccount;
import dev.jwtly10.liveapi.model.strategy.LiveStrategy;
import dev.jwtly10.liveapi.model.strategy.LiveStrategyConfig;
import dev.jwtly10.liveapi.repository.broker.BrokerAccountRepository;
import dev.jwtly10.liveapi.repository.strategy.LiveStrategyRepository;
import dev.jwtly10.shared.auth.utils.SecurityUtils;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import dev.jwtly10.shared.tracking.TrackingService;
import dev.jwtly10.shared.tracking.UserAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * <p>
     * A utility method for the Algotrade4j Frontend that will return all live strategies
     * INCLUDING strategy config defaults for any strategies that may be missing params
     * This is used to provide an easy way for users to update 'outdated' strategies, with missing config params
     * We also append metadata like type and enumValues, so the frontend can display the correct input field types
     * </p>
     * <p>
     * Essentially, using this endpoint to get strategy data, helps to keep configuration up to date.
     * The strategy will still fail to run, but at least by default, editing a strategy will update configuration to the latest defaults
     * </p>
     *
     * @return List of LiveStrategies including any missing run params
     */
    public List<LiveStrategy> getLiveStrategiesWithMissingRunParamsAndMetaData() {
        List<LiveStrategy> liveStrategies = getNonHiddenLiveStrategies();

        // For each strategy, check if the run params are missing
        for (LiveStrategy liveStrategy : liveStrategies) {
            // Create a map of the run params
            Map<String, String> liveRunParams = new HashMap<>();
            liveStrategy.getConfig().getRunParams().forEach(param ->
                    liveRunParams.put(param.getName(), param.getValue())
            );

            // Get the parameters for the strategy
            Strategy instance;
            try {
                instance = StrategyReflectionUtils.getStrategyFromClassName(liveStrategy.getConfig().getStrategyClass(), null);
            } catch (Exception e) {
                // If we fail here, it's hard to recover, the user needs to regenerate the strategy
                // But this error should be picked up by the validation job in live strategy @PostConstruct logic
                log.error("Error getting strategy instance: {}", e.getMessage(), e);
                continue;
            }

            try {
                // Required to get defaults from @Parameter annotations
                ParameterHandler.initialize(instance);
            } catch (Exception ex) {
                //  We can't recover if validation of the parameters fails outright - as we have already validated configuration on init & save
                // So we just continue, and let the user re-create the strategy, if they want to update it
                log.warn("Error initializing strategy parameters: {}", ex.getMessage());
                continue;
            }

            Map<String, ParameterHandler.ParameterInfo> defaultRunParams = new HashMap<>();
            ParameterHandler.getParameters(instance).forEach(param ->
                    defaultRunParams.put(param.getName(), param)
            );

            // If missing params, add them, else add type and enumValues to the existing params
            for (Map.Entry<String, ParameterHandler.ParameterInfo> entry : defaultRunParams.entrySet()) {
                // Add missing
                if (!liveRunParams.containsKey(entry.getKey())) {
                    liveStrategy.getConfig().getRunParams().add(
                            LiveStrategyConfig.RunParameter.builder()
                                    .name(entry.getKey())
                                    .value(entry.getValue().getValue())
                                    .description(entry.getValue().getDescription())
                                    .group(entry.getValue().getGroup())
                                    .type(entry.getValue().getType())
                                    .enumValues(entry.getValue().getEnumValues())
                                    .build()
                    );
                } else {
                    // Update latest type, enumValues, group and description
                    liveStrategy.getConfig().getRunParams().stream()
                            .filter(param -> param.getName().equals(entry.getKey()))
                            .findFirst()
                            .ifPresent(param -> {
                                param.setType(entry.getValue().getType());
                                param.setEnumValues(entry.getValue().getEnumValues());
                                param.setGroup(entry.getValue().getGroup());
                                param.setDescription(entry.getValue().getDescription());
                            });
                }

            }
        }

        return liveStrategies;
    }

    public List<LiveStrategy> getNonHiddenLiveStrategies() {
        log.trace("Fetching all non-hidden live strategies");
        return liveStrategyRepository.findLiveStrategiesByHiddenIsFalse();
    }

    public List<LiveStrategy> getActiveLiveStrategies() {
        log.trace("Fetching all active live strategies");
        return liveStrategyRepository.findLiveStrategiesByHiddenIsFalseAndActiveIsTrue();
    }

    public void setErrorMessage(LiveStrategy liveStrategy, String errorMessage) {
        liveStrategy.setLastErrorMsg(errorMessage);
        liveStrategyRepository.save(liveStrategy);
    }

    public void clearErrorMessage(LiveStrategy liveStrategy) {
        liveStrategy.setLastErrorMsg(null);
        liveStrategyRepository.save(liveStrategy);
    }

    public LiveStrategy updateStrategyStats(String liveStrategyId, Stats stats) {
        log.trace("Updating live strategy stats for strategy ID: {}", liveStrategyId);

        // Validate that the LiveStrategy exists
        LiveStrategy liveStrategy = liveStrategyRepository.findByStrategyName(liveStrategyId)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        liveStrategy.setStats(stats);
        return liveStrategyRepository.save(liveStrategy);
    }

    public LiveStrategy deactivateStrategy(String strategyName) {
        log.info("Deactivating live strategy: {}", strategyName);

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findByStrategyName(strategyName)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        if (!liveStrategy.isActive()) {
            log.warn("Strategy is already deactivated");
        }

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
            throw new ApiException("Strategy with the same name already exists (It may be hidden/deleted)", ErrorType.BAD_REQUEST);
        }

        try {
            strategy.getConfig().validate();
        } catch (Exception e) {
            log.error("Invalid live strategy configuration: {}", e.getMessage(), e);
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

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        // Toggle the active strategy
        liveStrategy.setActive(!liveStrategy.isActive());

        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.LIVE_STRATEGY_TOGGLE,
                Map.of(
                        "strategyId", id,
                        "strategyName", liveStrategy.getStrategyName(),
                        "toggledOn", liveStrategy.isActive()
                )
        );

        return liveStrategyRepository.save(liveStrategy);
    }

    public void deleteStrategy(Long id) {
        // We don't actually delete the strategy, we just deactivate it and set hidden
        log.info("Deleting live strategy: {}", id);

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.LIVE_STRATEGY_DELETE,
                Map.of(
                        "strategyId", id,
                        "strategyName", liveStrategy.getStrategyName()
                )
        );

        if (liveStrategy.isActive()) {
            throw new ApiException("Cannot delete an active strategy", ErrorType.BAD_REQUEST);
        }

        liveStrategy.setHidden(true);

        liveStrategyRepository.save(liveStrategy);
    }

    public LiveStrategy updateLiveStrategy(Long id, LiveStrategy updatedStratConfig) {
        log.info("Updating live strategy: {}", updatedStratConfig.getStrategyName());

        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.LIVE_STRATEGY_EDIT,
                Map.of(
                        "strategyId", id,
                        "strategyName", updatedStratConfig.getStrategyName(),
                        "config", updatedStratConfig.getConfig()
                )
        );

        // Find the strategy in the database
        LiveStrategy liveStrategy = liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));

        if (liveStrategy.isActive()) {
            // If we allow real time updating of strategies while they are live, we would need to coordinate with
            // the executor repository to shut down all threads that a strategy is using.
            // This is a lot of maintenance overhead, which isnt worth the QOL improvement.
            // We will require strategies must not be active to update them.
            throw new ApiException("Cannot update an active strategy", ErrorType.BAD_REQUEST);
        }

        LiveStrategy existingStrategy = liveStrategyRepository.findByStrategyName(updatedStratConfig.getStrategyName()).orElse(null);
        if (existingStrategy != null && !existingStrategy.getId().equals(id)) {
            throw new ApiException("Strategy with the same name already exists (It may be hidden/deleted)", ErrorType.BAD_REQUEST);
        }

        try {
            updatedStratConfig.getConfig().validate();
        } catch (Exception e) {
            log.error("Invalid live strategy configuration: {}", e.getMessage(), e);
            throw new ApiException("Invalid live strategy configuration: " + e.getMessage(), ErrorType.BAD_REQUEST);
        }

        // Update the possible values that we allow for updating
        liveStrategy.setStrategyName(updatedStratConfig.getStrategyName().trim());
        liveStrategy.setTelegramChatId(updatedStratConfig.getTelegramChatId().trim());
        liveStrategy.setConfig(updatedStratConfig.getConfig());

        // Save the strategy to the database
        return liveStrategyRepository.save(liveStrategy);
    }

    public Optional<LiveStrategy> getActiveStrategy(Long strategyId) {
        log.info("Getting active live strategy with id: {}", strategyId);
        return liveStrategyRepository.findByIdAndActiveIsTrue(strategyId);
    }

    public LiveStrategy getLiveStrategy(Long id) {
        log.info("Getting live strategy with id: {}", id);
        return liveStrategyRepository.findById(id)
                .orElseThrow(() -> new ApiException("Live strategy not found", ErrorType.NOT_FOUND));
    }
}