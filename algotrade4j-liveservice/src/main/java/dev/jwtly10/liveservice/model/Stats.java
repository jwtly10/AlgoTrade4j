package dev.jwtly10.liveservice.model;

import lombok.Data;

@Data
public class Stats {
    private double accountBalance;
    private double openTradeProfit;
    private double profit;
    private double totalTrades;
    private double winRate;
    private double profitFactor;
    private double sharpeRatio;
}