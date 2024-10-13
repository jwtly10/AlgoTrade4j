package dev.jwtly10.marketdata.impl.mt5.models;

public record Mt5Login(
        int accountId,
        String password,
        String server,
        String path
) {
}