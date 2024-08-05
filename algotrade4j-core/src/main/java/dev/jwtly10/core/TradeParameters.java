package dev.jwtly10.core;

import lombok.Data;

@Data
public class TradeParameters {
    private String symbol;
    private Number quantity;
    private Number entryPrice;
    private Number stopLoss;
    private Number takeProfit;
    private Number trailingStop;
    private Number riskPercentage;
    private Number riskRatio;
    private Number balanceToRisk;
}