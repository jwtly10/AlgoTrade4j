package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultTradeStateManager implements TradeStateManager {

    @Override
    public void updateTradeStates(AccountManager accountManager, TradeManager tradeManager, Tick tick) {
        tradeManager.getOpenTrades().values().forEach(trade -> {
            updateTradeProfitLoss(trade, tick);
            checkAndExecuteStopLossTakeProfit(trade, tradeManager, tick);
        });
        updateAccountBalanceAndEquity(accountManager, tradeManager);
    }

    private void updateTradeProfitLoss(Trade trade, Tick tick) {
        Number currentPrice = trade.isLong() ? tick.getBid() : tick.getAsk();
        Number priceDifference = trade.isLong()
                ? currentPrice.subtract(trade.getEntryPrice())
                : trade.getEntryPrice().subtract(currentPrice);

        Number profit = priceDifference.multiply(trade.getQuantity().getValue());
        trade.setProfit(profit);
        log.debug("Updating trade profit/loss for trade id: {}. Profit: {}", trade.getId(), trade.getProfit());
        log.debug("Current price: {}, entry price: {}", currentPrice, trade.getEntryPrice());
    }

    private void checkAndExecuteStopLossTakeProfit(Trade trade, TradeManager tradeManager, Tick tick) {
        if (hasHitStopLoss(trade, tick)) {
            log.debug("Stop loss hit for trade id : {}. SL at: {}, current tick at: {}. Loss: {}", trade.getId(), trade.getStopLoss(), trade.isLong() ? tick.getBid() : tick.getAsk(), trade.getProfit());
            tradeManager.closePosition(trade.getId());
        } else if (hasHitTakeProfit(trade, tick)) {
            log.debug("Take profit hit for trade id : {}. TP at: {}, current tick at: {}. Profit: {}", trade.getId(), trade.getTakeProfit(), trade.isLong() ? tick.getBid() : tick.getAsk(), trade.getProfit());
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

    private boolean hasHitTakeProfit(Trade trade, Tick tick) {
        if (trade.getTakeProfit() == null) {
            return false;
        }

        if (trade.isLong()) {
            return tick.getBid().isGreaterThan(trade.getTakeProfit()) || tick.getBid().isEquals(trade.getTakeProfit());
        } else {
            return tick.getAsk().isLessThan(trade.getTakeProfit()) || tick.getAsk().isEquals(trade.getTakeProfit());
        }
    }

    private void updateAccountBalanceAndEquity(AccountManager accountManager, TradeManager tradeManager) {
        Number totalProfit = tradeManager.getAllTrades().values().stream()
                .map(Trade::getProfit)
                .reduce(Number.ZERO, Number::add);

        // Calculate current balance by adding total profit to initial balance
        Number initialBalance = accountManager.getInitialBalance();
        Number currentBalance = initialBalance.add(totalProfit);

        log.debug("Initial balance: {}, total profit: {}, current balance: {}", initialBalance, totalProfit, currentBalance);
        // Set the current balance
        accountManager.setBalance(currentBalance);

        // Calculate and set equity (balance + unrealized profit/loss from open trades)
        Number unrealizedProfitLoss = tradeManager.getAllTrades().values().stream()
                .filter(trade -> trade.getClosePrice().equals(Number.ZERO)) // Filter for open trades
                .map(Trade::getProfit)
                .reduce(Number.ZERO, Number::add);
        Number equity = currentBalance.add(unrealizedProfitLoss);
        accountManager.setEquity(equity);
        log.debug("Unrealized profit/loss: {}, Equity: {}", unrealizedProfitLoss, equity);
    }
}