package dev.jwtly10.core.indicators;

import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.model.Bar;

/**
 * Represents a financial indicator used in technical analysis.
 * Indicators are tools used to analyze price movements and trends in financial markets.
 */
public interface Indicator {
    /**
     * Updates the indicator with a new price bar.
     * This method should be called for each new bar in chronological order.
     *
     * @param bar the new price bar to be incorporated into the indicator's calculation
     */
    void update(Bar bar);

    /**
     * Retrieves the current value of the indicator.
     * This value represents the most recent calculation of the indicator.
     *
     * @return the current value of the indicator as a Price object
     */
    Number getValue();

    /**
     * Retrieves a historical value of the indicator.
     * The index represents the number of bars back from the most recent update.
     *
     * @param index the number of bars back from the most recent update (0 is the most recent)
     * @return the historical value of the indicator at the specified index as a Price object
     * @throws IndexOutOfBoundsException if the index is negative or exceeds the number of available values
     */
    Number getValue(int index);

    /**
     * Retrieves the name of the indicator.
     * This typically includes the indicator's abbreviation and any relevant parameters.
     *
     * @return a string representation of the indicator's name
     */
    String getName();

    /**
     * Checks if the indicator has enough data to produce a valid value.
     * Some indicators require a certain number of bars before they can produce meaningful results.
     *
     * @return true if the indicator has enough data to produce a valid value, false otherwise
     */
    boolean isReady();

    /**
     * Retrieves the number of periods (bars) required for the indicator to produce a valid value.
     * This represents the minimum number of bars that must be provided before the indicator is ready.
     *
     * @return the number of periods required for the indicator to produce a valid value
     */
    int getRequiredPeriods();

    /**
     * Sets the event publisher for the indicator.
     * The event publisher is used to publish events related to the indicator's calculations.
     *
     * @param eventPublisher the event publisher to be used by the indicator
     */
    void setEventPublisher(EventPublisher eventPublisher);

    /**
     * Sets the strategy ID for the indicator.
     * The strategy ID is used to uniquely identify the strategy that owns the indicator.
     *
     * @param strategyId the unique identifier of the strategy that owns the indicator
     */
    void setStrategyId(String strategyId);
}