package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;

public class DefaultTradeStateManager implements TradeStateManager {

    @Override
    public void updateTradeStates(AccountManager accountManager, TradeManager tradeManager, Tick tick) {
//        tradeManager.getOpenTrades().values().forEach(trade -> {
//            updateTradeProfitLoss(trade, tick);
//            checkAndExecuteStopLoss(trade, tradeManager, tick);
//        });
//        updateAccountBalanceAndEquity(accountManager, tradeManager);
    }

    private void updateTradeProfitLoss(Trade trade, Tick tick) {
        Number currentPrice = trade.isLong() ? tick.getBid() : tick.getAsk();
        Number priceDifference = trade.isLong()
                ? currentPrice.subtract(trade.getEntryPrice())
                : trade.getEntryPrice().subtract(currentPrice);

        Number profit = priceDifference.multiply(trade.getQuantity().getValue());
        trade.setProfit(profit);
    }

    private void checkAndExecuteStopLoss(Trade trade, TradeManager tradeManager, Tick tick) {
        if (hasHitStopLoss(trade, tick)) {
            tradeManager.closePosition(trade.getId());
        }
    }

    private boolean hasHitStopLoss(Trade trade, Tick tick) {
        if (trade.getStopLoss() == null) {
            return false;
        }

        if (trade.isLong()) {
            return tick.getBid().isLessThan(trade.getStopLoss()) || tick.getBid().isEquals(trade.getStopLoss());
        } else {
            return tick.getAsk().isGreaterThan(trade.getStopLoss()) || tick.getAsk().isEquals(trade.getStopLoss());
        }
    }

    private void updateAccountBalanceAndEquity(AccountManager accountManager, TradeManager tradeManager) {
        Number totalProfit = tradeManager.getOpenTrades().values().stream()
                .map(Trade::getProfit)
                .reduce(Number.ZERO, Number::add);

        Number initialBalance = accountManager.getInitialBalance();
        Number currentBalance = initialBalance.add(totalProfit);

        // Set the current balance
        accountManager.setBalance(currentBalance);

        // Calculate and set equity (balance + unrealized profit/loss)
        Number unrealizedProfitLoss = tradeManager.getOpenTrades().values().stream()
                .map(Trade::getProfit)
                .reduce(Number.ZERO, Number::add);
        Number equity = currentBalance.add(unrealizedProfitLoss);
        accountManager.setEquity(equity);
    }
}