package dev.jwtly10.core;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Trade {
    private final String id;
    private final String symbol;
    private final Number quantity;
    private final Number entryPrice;
    private final Number stopLoss;
    private final Number takeProfit;
    private final boolean isLong;

    public Trade(String symbol, Number quantity, Number entryPrice, Number stopLoss, Number takeProfit, boolean isLong) {
        this.id = UUID.randomUUID().toString();
        this.symbol = symbol;
        this.quantity = quantity;
        this.entryPrice = entryPrice;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.isLong = isLong;
    }

    public boolean isLong() {
        return isLong;
    }
}