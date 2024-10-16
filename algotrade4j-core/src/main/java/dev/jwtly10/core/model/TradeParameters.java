package dev.jwtly10.core.model;

import dev.jwtly10.core.exception.InvalidTradeException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Represents the parameters for a trade in the trading system.
 */
@Data
@Slf4j
public class TradeParameters {
    /**
     * The instrument associated with the trade.
     */
    private Instrument instrument;

    /**
     * The quantity of the trade.
     */
    private double quantity;

    /**
     * The entry price of the trade.
     */
    private Number entryPrice;

    /**
     * The stop loss price of the trade.
     */
    private Number stopLoss;

    /**
     * The take profit price of the trade.
     */
    private Number takeProfit;

    // TODO: This needs to be considered very very carefully, so will not support for now
//    /**
//     * The trailing stop price of the trade.
//     */
//    private Number trailingStop;

    /**
     * The risk percentage of the trade.
     */
    private double riskPercentage;

    /**
     * The risk ratio of the trade.
     */
    private double riskRatio;

    /**
     * The balance to risk for the trade.
     */
    private double balanceToRisk;

    /**
     * A specific flag indicating if the trade is long.
     */
    private boolean isLong;

    /**
     * Ideal open time of the trade
     */
    private ZonedDateTime openTime;


    /**
     * There are 2 options to open a trade, RAW or CALCULATED.
     * RAW trades require the user to provide all the necessary parameters for the trade.
     * [entryPrice, stopLoss, takeProfit, quantity] - so they must calculate everything themselves
     * CALCULATED trades require the user to provide the following parameters:
     * [entryPrice (or just market), riskPercentage, riskRatio, balanceToRisk, stopLoss] - the system will calculate the rest
     */
    public boolean useRaw() {
        return entryPrice != null && stopLoss != null && takeProfit != null && quantity != 0 && openTime != null;
    }

    /**
     * Uses the parameters of the instance to generate a trade object.
     *
     * @return the trade object
     */
    public Trade createTrade() {
        Trade trade;
        if (useRaw()) {
            log.debug("Using raw parameters to open trade");
            if (quantity < 0) {
                log.warn("Quantity cannot be below 0");
                throw new InvalidTradeException(String.format("Quantity cannot be below 0 for the new trade opened @ %s. Value was %s", openTime, this.quantity));
            }
            trade = new Trade(instrument, quantity, entryPrice, openTime, stopLoss, takeProfit, isLong);
        } else {
            double balance = balanceToRisk;
            log.trace("Selected balance: {}", balance);

            double riskInPercent = riskPercentage / 100;
            double riskAmount = balance * riskInPercent;
            log.trace("Risk amount calculation: {} * {} = {}", balance, riskInPercent, riskAmount);

            Number stopLossDistance = isLong ? entryPrice.subtract(stopLoss).abs() :
                    stopLoss.subtract(entryPrice).abs();
            log.trace("Stop loss distance calculation: |{} - {}| = {}",
                    isLong ? entryPrice : stopLoss,
                    isLong ? stopLoss : entryPrice, stopLossDistance);

            // If quantity == 0 ( the default of a double ) then we should calculate the expected quantity based on the risk amount
            double quantity = this.quantity != 0 ? this.quantity :
                    riskAmount / stopLossDistance.getValue().doubleValue();
            // TODO: This rounding workaround is based on issue https://onenr.io/0ERPr99n6QW -
            // It works as we are just testing the prod impl on NAS100USD for OANDA. But this may not work for Forex pairs
            // We need a way to pass context about the broker and max units allowed
            // And use that to round the quantity to the nearest allowed unit
            // Note this causes the Integration test to fail.
            // Only making this change because the goal is to test the prod impl on NAS100USD
            quantity = Math.round(quantity * 10.0) / 10.0;
            log.trace("Quantity calculation: {} / {} = {}", riskAmount, stopLossDistance.getValue(), quantity);

            Number takeProfit = this.takeProfit != null ? this.takeProfit :
                    (isLong ? entryPrice.add(stopLossDistance.multiply(BigDecimal.valueOf(riskRatio))) :
                            entryPrice.subtract(stopLossDistance.multiply(BigDecimal.valueOf(riskRatio))));
            log.trace("Take profit calculation: {} {} ({} * {}) = {}",
                    entryPrice, isLong ? "+" : "-", stopLossDistance, riskRatio, takeProfit);

            quantity = Math.floor(quantity * 100) / 100;
            log.trace("Final quantity after rounding down to 2 decimal places: {}", quantity);

            if (stopLoss.isLessThan(Number.ZERO) || takeProfit.isLessThan(Number.ZERO)) {
                String reason = stopLoss.isLessThan(Number.ZERO) ? "STOP LOSS" : "TAKE PROFIT";
                log.warn("{} cannot be below 0", reason);
                throw new InvalidTradeException(String.format("%s cannot be below 0 for the new trade opened @ %s. Value was %s", reason, openTime,
                        reason.equals("STOP LOSS") ? stopLoss : takeProfit));
            }

            if (quantity < 0) {
                log.warn("Quantity cannot be below 0");
                throw new InvalidTradeException(String.format("Quantity cannot be below 0 for the new trade opened @ %s. Value was %s", openTime, quantity));
            }

            trade = new Trade(instrument, quantity, entryPrice, openTime,
                    stopLoss, takeProfit, isLong);
        }

        return trade;
    }
}