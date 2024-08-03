package dev.jwtly10.core;

/**
 * The PriceFeed interface provides methods to retrieve various price-related
 * information for financial instruments identified by their symbol.
 */
public interface PriceFeed {
    /**
     * Retrieves the current bid price for the specified symbol.
     *
     * @param symbol The identifier for the financial instrument.
     * @return The bid price as a Number.
     */
    Number getBid(String symbol);

    /**
     * Retrieves the current ask price for the specified symbol.
     *
     * @param symbol The identifier for the financial instrument.
     * @return The ask price as a Number.
     */
    Number getAsk(String symbol);

    /**
     * Retrieves the opening price for the specified symbol in the current trading period.
     *
     * @param symbol The identifier for the financial instrument.
     * @return The opening price as a Number.
     */
    Number getOpen(String symbol);

    /**
     * Retrieves the highest price for the specified symbol in the current trading period.
     *
     * @param symbol The identifier for the financial instrument.
     * @return The highest price as a Number.
     */
    Number getHigh(String symbol);

    /**
     * Retrieves the lowest price for the specified symbol in the current trading period.
     *
     * @param symbol The identifier for the financial instrument.
     * @return The lowest price as a Number.
     */
    Number getLow(String symbol);

    /**
     * Retrieves the closing price for the specified symbol in the most recent completed trading period.
     *
     * @param symbol The identifier for the financial instrument.
     * @return The closing price as a Number.
     */
    Number getClose(String symbol);
}