package dev.jwtly10.core.model;

import lombok.Data;

/**
 * Represents the parameters for a trade in the trading system.
 */
@Data
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

    /**
     * The trailing stop price of the trade.
     */
    private Number trailingStop;

    /**
     * The risk percentage of the trade.
     */
    private double riskPercentage;

    /**
     * The risk ratio of the trade.
     */
    private Number riskRatio;

    /**
     * The balance to risk for the trade.
     */
    private double balanceToRisk;
}