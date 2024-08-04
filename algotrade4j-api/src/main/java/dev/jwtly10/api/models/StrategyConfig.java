package dev.jwtly10.api.models;

import dev.jwtly10.core.Number;
import lombok.Data;

import java.util.List;

@Data
public class StrategyConfig {
    private String strategyId;
    private List<String> subscriptions;
    private Number initialCash;
    private int barSeriesSize;
}