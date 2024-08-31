package dev.jwtly10.core.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;

/**
 * Represents a trade in the trading system.
 */
@Getter
@Setter
@ToString
public class Trade {
    private static Integer idCounter = 0;

    /**
     * The unique identifier of the trade.
     */
    private final Integer id;

    /**
     * The instrument associated with the trade.
     */
    private final Instrument instrument;

    /**
     * The quantity of the trade.
     */
    private final Number quantity;

    /**
     * The entry price of the trade.
     */
    private final Number entryPrice;

    /**
     * The stop loss price of the trade.
     */
    private final Number stopLoss;

    /**
     * The take profit price of the trade.
     */
    private final Number takeProfit;

    /**
     * Indicates whether the trade is a long position.
     */
    private final boolean isLong;

    /**
     * The profit of the trade.
     */
    private double profit = 0;

    /**
     * The close price of the trade.
     */
    private Number closePrice = Number.ZERO;

    /**
     * The open time of the trade.
     */
    private ZonedDateTime openTime;

    /**
     * The close time of the trade.
     */
    private ZonedDateTime closeTime;

    /**
     * Constructs a Trade with the specified parameters.
     * Generates a unique identifier for the trade.
     *
     * @param instrument the instrument associated with the trade
     * @param quantity   the quantity of the trade
     * @param entryPrice the entry price of the trade
     * @param openTime   the open time of the trade
     * @param stopLoss   the stop loss price of the trade
     * @param takeProfit the take profit price of the trade
     * @param isLong     indicates whether the trade is a long position
     */
    public Trade(Instrument instrument, Number quantity, Number entryPrice, ZonedDateTime openTime, Number stopLoss, Number takeProfit, boolean isLong) {
        this.id = ++idCounter;
        this.instrument = instrument;
        this.quantity = quantity;
        this.entryPrice = entryPrice;
        this.openTime = openTime;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.isLong = isLong;
    }

    /**
     * Constructs a Trade with the specified parameters including an existing ID.
     *
     * @param id         the unique identifier of the trade
     * @param instrument the instrument associated with the trade
     * @param quantity   the quantity of the trade
     * @param openTime   the open time of the trade
     * @param entryPrice the entry price of the trade
     * @param stopLoss   the stop loss price of the trade
     * @param takeProfit the take profit price of the trade
     * @param isLong     indicates whether the trade is a long position
     */
    public Trade(int id, Instrument instrument, Number quantity, ZonedDateTime openTime, Number entryPrice, Number stopLoss, Number takeProfit, boolean isLong) {
        this.id = id;
        this.instrument = instrument;
        this.quantity = quantity;
        this.entryPrice = entryPrice;
        this.openTime = openTime;
        this.stopLoss = stopLoss;
        this.takeProfit = takeProfit;
        this.isLong = isLong;
        idCounter = Math.max(idCounter, id);
    }

    /**
     * Returns whether the trade is a long position.
     *
     * @return true if the trade is a long position, false otherwise
     */
    public boolean isLong() {
        return isLong;
    }
}
