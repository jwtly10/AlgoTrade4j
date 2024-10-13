package dev.jwtly10.marketdata.impl.mt5.models;

import dev.jwtly10.core.model.Trade;

public record Mt5Trade(

) {
    public Trade toTrade() {
        throw new RuntimeException("Not implemented yet");
    }
}