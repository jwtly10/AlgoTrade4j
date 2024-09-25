package dev.jwtly10.liveapi.model;

import lombok.Data;

@Data
public class Stats {
    private double accountBalance;
    private double openTradeProfit;
    private int openTrades;
    private double profit;
    private int totalTrades;
    private double winRate;
    private double profitFactor;
    private double sharpeRatio;
}