package dev.jwtly10.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BacktestTradeExecutor implements TradeExecutor {
    private final Map<String, Trade> trades;
    @Getter
    private final Account account;
    private Bar currentBar;

    public BacktestTradeExecutor(Number initialCash) {
        this.account = new Account(initialCash);
        this.trades = new HashMap<>();
    }

    @Override
    public String openLongPosition(String symbol, Number quantity, Number entryPrice, Number stopLoss, Number takeProfit) {
        Trade trade = new Trade(symbol, quantity, entryPrice, stopLoss, takeProfit, true);
        trades.put(trade.getId(), trade);
        account.updateBalance(account.getBalance().subtract(quantity.multiply(entryPrice.getValue())));
        updateAccountState();
        return trade.getId();
    }

    @Override
    public String openShortPosition(String symbol, Number quantity, Number entryPrice, Number stopLoss, Number takeProfit) {
        Trade trade = new Trade(symbol, quantity, entryPrice, stopLoss, takeProfit, false);
        trades.put(trade.getId(), trade);
        account.updateBalance(account.getBalance().add(quantity.multiply(entryPrice.getValue())));
        updateAccountState();
        return trade.getId();
    }

    @Override
    public void closePosition(String tradeId) {
        Trade trade = trades.remove(tradeId);
        if (trade == null) {
            throw new IllegalArgumentException("Trade not found: " + tradeId);
        }

        if (currentBar == null) {
            throw new IllegalStateException("Current bar is not set");
        }

        // In a real implementation, we would also consider the spread and commission TODO: Implement fake/ranging spread for backtesting
        // This would be done by calling the broker API and using data to determine the current price
        // During backtesting. We will work off of a new bar opening .

        // TODO: Review how we get close price during backtest
        // For now we are using the open price of the current bar to calculate the profit/loss
        // This is because we are working with historical data and we don't have access to the current price (for now)
        // This should be reviewed if we implement lower tick data and do calculations to fit the period
        // (This gives us more precision but increase the complexity of the system)

        if (trade.isLong()) {
            account.updateBalance(account.getBalance().add(trade.getQuantity().multiply(currentBar.getOpen().getValue())));
        } else {
            account.updateBalance(account.getBalance().subtract(trade.getQuantity().multiply(currentBar.getOpen().getValue())));
        }
        updateAccountState();
    }

    @Override
    public Trade getTrade(String tradeId) {
        return trades.get(tradeId);
    }

    @Override
    public Number getPosition(String symbol) {
        return trades.values().stream()
                .filter(t -> t.getSymbol().equals(symbol))
                .map(t -> t.isLong() ? t.getQuantity() : t.getQuantity().multiply(new Number("-1").getValue()))
                .reduce(Number.ZERO, Number::add);
    }

    @Override
    public Number getBalance() {
        return account.getBalance();
    }

    @Override
    public Number getEquity() {
        return account.getEquity();
    }

    @Override
    public Number getOpenPositionValue() {
        return account.getOpenPositionValue();
    }

    public void updateTrades(Bar bar) {
        currentBar = bar;

        String symbol = bar.getSymbol();
        Number high = bar.getHigh();
        Number low = bar.getLow();

        List<String> tradesToClose = new ArrayList<>();

        for (Trade trade : trades.values()) {
            if (trade.getSymbol().equals(symbol)) {
                if (trade.isLong()) {
                    if (low.isLessThan(trade.getStopLoss()) || high.isGreaterThan(trade.getTakeProfit())) {
                        tradesToClose.add(trade.getId());
                    }
                } else {
                    if (high.isGreaterThan(trade.getStopLoss()) || low.isLessThan(trade.getTakeProfit())) {
                        tradesToClose.add(trade.getId());
                    }
                }
            }
        }

        for (String tradeId : tradesToClose) {
            closePosition(tradeId);
        }

        updateAccountState();
    }

    private void updateAccountState() {
        Number openPositionValue = trades.values().stream()
                .map(trade -> trade.getQuantity().multiply(trade.getEntryPrice().getValue()))
                .reduce(Number.ZERO, Number::add);

        account.updateOpenPositionValue(openPositionValue);
        account.updateEquity(account.getBalance().add(openPositionValue));
    }
}