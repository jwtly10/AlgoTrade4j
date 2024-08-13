package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.event.AnalysisEvent;
import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.event.EventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OptimisationResultListener implements EventListener {
    private final Map<String, AnalysisEvent> results = new ConcurrentHashMap<>();

    @Override
    public void onEvent(BaseEvent event) {
        if (event instanceof AnalysisEvent analysisEvent) {
            results.put(analysisEvent.getStrategyId(), analysisEvent);
        }
    }

    @Override
    public void onError(String strategyId, Exception e) {
        log.error("Error in strategy: {}", strategyId, e);
    }

    public Map<String, AnalysisEvent> getResults() {
        return new HashMap<>(results);
    }

    public void clear() {
        results.clear();
    }
}