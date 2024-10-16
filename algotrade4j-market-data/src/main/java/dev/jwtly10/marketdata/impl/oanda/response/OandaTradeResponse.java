package dev.jwtly10.marketdata.impl.oanda.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.marketdata.impl.oanda.models.OandaTrade;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaTradeResponse(List<OandaTrade> trades, String lastTransactionID) {
}