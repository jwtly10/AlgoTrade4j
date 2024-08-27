package dev.jwtly10.api.service;

import dev.jwtly10.api.exception.ErrorType;
import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.models.StrategyConfig;
import dev.jwtly10.api.utils.ConfigConverter;
import dev.jwtly10.core.data.DataProvider;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.SyncEventPublisher;
import dev.jwtly10.core.optimisation.OptimisationConfig;
import dev.jwtly10.core.optimisation.OptimisationExecutor;
import dev.jwtly10.core.optimisation.OptimisationResult;
import dev.jwtly10.marketdata.common.ExternalDataClient;
import dev.jwtly10.marketdata.common.ExternalDataProvider;
import dev.jwtly10.marketdata.dataclients.OandaDataClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OptimisationManager {
    private Map<String, OptimisationResult> optimisationResults = new ConcurrentHashMap<>();

    @Value("${oanda.api.key}")
    private String oandaApiKey;
    @Value("${oanda.account.id}")
    private String oandaAccountId;
    @Value("${oanda.api.url}")
    private String oandaApiUrl;


    public OptimisationManager() {
    }

    public void validateOptimisationConfig(StrategyConfig config) throws IllegalArgumentException {
        OptimisationConfig optimisationConfig = ConfigConverter.convertToOptimisationConfig(config);
        optimisationConfig.validate();
    }

    public void startOptimisation(StrategyConfig strategyConfig, String optimisationId) {
        OptimisationConfig config = ConfigConverter.convertToOptimisationConfig(strategyConfig);

        OandaClient oandaClient = new OandaClient(oandaApiUrl, oandaApiKey, oandaAccountId);
        ExternalDataClient externalDataClient = new OandaDataClient(oandaClient);

        // Ensure utc
        ZoneId utcZone = ZoneId.of("UTC");
        ZonedDateTime from = config.getTimeframe().getFrom().atZone(utcZone).withZoneSameInstant(utcZone);
        ZonedDateTime to = config.getTimeframe().getTo().atZone(utcZone).withZoneSameInstant(utcZone);
        DataProvider dataProvider = new ExternalDataProvider(externalDataClient, config.getInstrument(), config.getSpread(), config.getPeriod(), from, to, 12345L);

        // This event publisher will ONLY be used for optimisation result listening.
        // It will not be used for external event publishing
        EventPublisher internalEventPublisher = new SyncEventPublisher();

        OptimisationExecutor optimisationExecutor = new OptimisationExecutor(internalEventPublisher, dataProvider);

        CompletableFuture.runAsync(() -> {
            try {
                OptimisationResult result = optimisationExecutor.runOptimisation(config);
                optimisationResults.put(optimisationId, result);
            } catch (Exception e) {
                log.error("Failed to run optimisation", e);
                throw new StrategyManagerException("Failed to start optimisation: " + e.getMessage(), ErrorType.BAD_REQUEST);
            }
        });
    }

    public OptimisationResult getResults(String optimisationId) {
        log.debug("All optimisation results: {}", optimisationResults);
        return optimisationResults.get(optimisationId);
    }
}