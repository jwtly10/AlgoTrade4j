package dev.jwtly10.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class Trade {
    private final String id;
    private final String symbol;
    private final Number quantity;
    private final Number entryPrice;
    private final Number stopLoss;
    private final Number takeProfit;
    private final boolean isLong;
    private Number profit = Number.ZERO;
    private Number closePrice = Number.ZERO;
    private ZonedDateTime openTime;
    private ZonedDateTime closeTime;

    public Trade(String symbol, Number quantity, Number entryPrice, ZonedDateTime openTime, Number stopLoss, Number takeProfit, boolean isLong) {
        this.id = UUID.randomUUID().toString();
        this.symbol = symbol;
        this.quantity = quantity;
        this.entryPrice = entryPrice;
        this.openTime = openTime;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.isLong = isLong;
    }

    public Trade(String id, String symbol, Number quantity, ZonedDateTime openTime, Number entryPrice, Number stopLoss, Number takeProfit, boolean isLong) {
        this.id = id;
        this.symbol = symbol;
        this.quantity = quantity;
        this.entryPrice = entryPrice;
        this.openTime = openTime;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.isLong = isLong;
    }

    public boolean isLong() {
        return isLong;
    }
}