package dev.jwtly10.marketdata.impl.oanda.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.marketdata.impl.oanda.models.OandaAccount;

/**
 * OandaAccountResponse
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaAccountResponse(OandaAccount account, String lastTransactionID) {
}