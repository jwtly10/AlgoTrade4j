package dev.jwtly10.marketdata.impl.mt5.request;

import dev.jwtly10.core.model.Number;

public record Mt5TradeRequest(
        String instrument,
        double quantity,
        Number entryPrice,
        Number stopLoss,
        Number takeProfit,
        double riskPercentage,
        double riskRatio,
        double balanceToRisk,
        boolean isLong,
        Long openTime
) {
}