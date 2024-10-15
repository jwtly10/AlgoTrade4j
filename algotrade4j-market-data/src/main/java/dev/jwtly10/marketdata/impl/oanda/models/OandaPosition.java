package dev.jwtly10.marketdata.impl.oanda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * OandaPosition
 * https://developer.oanda.com/rest-live-v20/position-df/#Position
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaPosition() {
}