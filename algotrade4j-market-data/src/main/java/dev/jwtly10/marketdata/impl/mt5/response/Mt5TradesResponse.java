package dev.jwtly10.marketdata.impl.mt5.response;

import dev.jwtly10.marketdata.impl.mt5.models.Mt5Trade;

import java.util.List;

public record Mt5TradesResponse(List<Mt5Trade> trades) {
}