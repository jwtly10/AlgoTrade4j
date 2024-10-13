package dev.jwtly10.marketdata.impl.oanda.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.marketdata.impl.oanda.models.PriceBucket;

import java.util.List;

/**
 * Represents a price response from the Oanda API.
 *
 * <p>For more information, see the
 * <a href="https://developer.oanda.com/rest-live-v20/pricing-df/#ClientPrice">Oanda API Documentation</a>.</p>
 *
 * @param type        the type of the price response
 * @param instrument  the instrument for which the price is provided
 * @param time        the time of the price response
 * @param tradeable   whether the instrument is tradeable
 * @param bids        the list of bid price buckets
 * @param asks        the list of ask price buckets
 * @param closeoutBid the closeout bid price
 * @param closeoutAsk the closeout ask price
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaPriceResponse(String type, String instrument, String time, boolean tradeable, List<PriceBucket> bids, List<PriceBucket> asks, String closeoutBid, String closeoutAsk) {
}