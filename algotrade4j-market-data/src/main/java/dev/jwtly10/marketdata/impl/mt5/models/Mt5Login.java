package dev.jwtly10.marketdata.impl.mt5.models;

public record Mt5Login(
        Integer accountId,
        String password,
        String server,
        String path
) {
}