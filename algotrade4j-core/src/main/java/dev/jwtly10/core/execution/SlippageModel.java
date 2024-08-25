package dev.jwtly10.core.execution;

import dev.jwtly10.core.model.Number;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlippageModel {
    private static final double NORMAL_SLIPPAGE_FACTOR = 0.2;
    private static final double HIGH_VOLATILITY_SLIPPAGE_FACTOR = 1.0;

    public Number calculateExecutionPrice(boolean isLong, Number stopLoss, Number takeProfit, Number ask, Number bid, boolean isHighVolatility) {
        double slippageFactor = isHighVolatility ? HIGH_VOLATILITY_SLIPPAGE_FACTOR : NORMAL_SLIPPAGE_FACTOR;

        if (isLong) {
            if (bid.isLessThan(stopLoss)) {
                // Stop loss hit for long trade
                return stopLoss.add(bid.subtract(stopLoss).multiply(new Number(slippageFactor)));
            } else if (bid.isGreaterThan(takeProfit)) {
                // Take profit hit for long trade
                var d = takeProfit.add(bid.subtract(takeProfit).multiply(new Number(slippageFactor)));
                return d;
            }
        } else {
            if (ask.isGreaterThan(stopLoss)) {
                // Stop loss hit for short trade
                return stopLoss.add(ask.subtract(stopLoss).multiply(new Number(slippageFactor)));
            } else if (ask.isLessThan(takeProfit)) {
                // Take profit hit for short trade
                return takeProfit.add(ask.subtract(takeProfit).multiply(new Number(slippageFactor)));
            }
        }
        return null;
    }
}