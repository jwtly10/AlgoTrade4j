package dev.jwtly10.marketdata.impl.mt5.response;

import dev.jwtly10.marketdata.impl.mt5.models.MT5Trade;

import java.util.List;

public record MT5TradesResponse(List<MT5Trade> trades) {
}