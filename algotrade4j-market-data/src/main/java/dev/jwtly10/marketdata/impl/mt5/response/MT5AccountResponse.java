package dev.jwtly10.marketdata.impl.mt5.response;

public record MT5AccountResponse(
        double assets,
        double balance,
        double commission_blocked,
        String company,
        double credit,
        String currency,
        int currency_digits,
        double equity,
        boolean fifo_close,
        int leverage,
        double liabilities,
        int limit_orders,
        long login,
        double margin,
        double margin_free,
        double margin_initial,
        double margin_level,
        double margin_maintenance,
        int margin_mode,
        double margin_so_call,
        int margin_so_mode,
        double margin_so_so,
        String name,
        double profit,
        String server,
        boolean trade_allowed,
        boolean trade_expert,
        int trade_mode
) {
}