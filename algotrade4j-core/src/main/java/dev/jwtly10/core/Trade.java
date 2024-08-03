package dev.jwtly10.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
public class Trade {
    private final String id;
    private final String symbol;
    private final Number quantity;
    private final Number entryPrice;
    @Setter
    private Number closePrice = Number.ZERO;
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

    public Trade(String id, String symbol, Number quantity, Number entryPrice, Number stopLoss, Number takeProfit, boolean isLong) {
        this.id = id;
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