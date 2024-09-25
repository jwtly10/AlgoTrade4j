package dev.jwtly10.marketdata.oanda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * https://developer.oanda.com/rest-live-v20/account-df/#Account
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaAccount(
        String id,
        String createdTime,
        String currency,
        String alias,
        String marginRate,
        String lastTransactionID,
        String balance,
        int openTradeCount,
        int openPositionCount,
        int pendingOrderCount,
        String pl,
        String resettablePL,
        String resettablePLTime,
        String financing,
        String commission,
        String dividendAdjustment,
        String guaranteedExecutionFees,
        List<Object> orders,
        List<OandaPosition> positions,
        List<OandaTrade> trades,
        String unrealizedPL,
        @JsonProperty("NAV") String nAV,
        String marginUsed,
        String marginAvailable,
        String positionValue,
        String marginCloseoutUnrealizedPL,
        String marginCloseoutNAV,
        String marginCloseoutMarginUsed,
        String marginCloseoutPositionValue,
        String marginCloseoutPercent,
        String withdrawalLimit,
        String marginCallMarginUsed,
        String marginCallPercent
) {
}