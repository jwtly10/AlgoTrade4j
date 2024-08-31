package dev.jwtly10.core.event.async;

import dev.jwtly10.core.event.BaseEvent;
import dev.jwtly10.core.model.IndicatorValue;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Event representing all indicator data from a given strategy run
 */
@Getter
public class AsyncIndicatorsEvent extends BaseEvent {
    private final Map<String, List<IndicatorValue>> indicators;

    public AsyncIndicatorsEvent(String strategyId, Instrument instrument, Map<String, List<IndicatorValue>> indicators) {
        super(strategyId, "ALL_INDICATORS", instrument);
        this.indicators = filterZeroValues(indicators);
    }

    private Map<String, List<IndicatorValue>> filterZeroValues(Map<String, List<IndicatorValue>> originalIndicators) {
        Map<String, List<IndicatorValue>> filteredIndicators = new HashMap<>();

        for (Map.Entry<String, List<IndicatorValue>> entry : originalIndicators.entrySet()) {
            List<IndicatorValue> filteredList = entry.getValue().stream()
                    .filter(iv -> iv.getValue() != 0)
                    .collect(Collectors.toList());

            if (!filteredList.isEmpty()) {
                filteredIndicators.put(entry.getKey(), filteredList);
            }
        }

        return filteredIndicators;
    }
}