package dev.jwtly10.marketdata.impl.mt5.request;

public record MT5TradeRequest(
        String instrument,
        Double quantity,
        Double entryPrice,
        Double stopLoss,
        Double takeProfit,
        Double riskPercentage,
        Double riskRatio,
        Double balanceToRisk,
        Boolean isLong,
        Long openTime
) {
}