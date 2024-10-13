package dev.jwtly10.marketdata.impl.oanda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Represents a candlestick in the Oanda API.
 *
 * <p>For more information, see the
 * <a href="https://developer.oanda.com/rest-live-v20/instrument-df/#Candlestick">Oanda API Documentation</a>.</p>
 *
 * @param time   the time of the candlestick
 * @param mid    the mid prices of the candlestick
 * @param volume the volume of the candlestick
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaCandle(String time, OandaCandleMid mid, int volume) {
}