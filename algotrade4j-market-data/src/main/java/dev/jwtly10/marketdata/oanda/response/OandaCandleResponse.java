package dev.jwtly10.marketdata.oanda.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.marketdata.oanda.models.OandaCandle;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaCandleResponse(List<OandaCandle> candles, String instrument, String granularity) {
}