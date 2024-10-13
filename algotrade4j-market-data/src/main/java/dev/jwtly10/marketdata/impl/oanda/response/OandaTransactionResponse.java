package dev.jwtly10.marketdata.impl.oanda.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * OandaTransactionResponse
 * https://developer.oanda.com/rest-live-v20/transaction-df/#Transaction
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaTransactionResponse() {
}