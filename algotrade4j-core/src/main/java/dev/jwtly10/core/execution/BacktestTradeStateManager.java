package dev.jwtly10.core.execution;

import dev.jwtly10.core.account.AccountManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.AccountEvent;
import dev.jwtly10.core.event.types.TradeEvent;
import dev.jwtly10.core.exception.RiskException;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.model.Trade;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BacktestTradeStateManager implements TradeStateManager {
    private final EventPublisher eventPublisher;
    private final String strategyId;
    private Tick lastTick = null;

    public BacktestTradeStateManager(String strategyId, EventPublisher eventPublisher) {
        this.strategyId = strategyId;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void updateTradeProfitStateOnTick(TradeManager tradeManager, Tick tick) {
        if (tick == null && lastTick == null) {
            // This will handles cases when the strategy first starts and there are no ticks to process
            log.trace("Tick data is null. Skipping hack update.");
            return;
        } else if (tick == null) {
            tick = lastTick;
        }
        this.lastTick = tick;
        log.trace("Current prices - Ask: {}, Bid: {}", tick.getAsk(), tick.getBid());

        Tick finalTick = tick;
        ZonedDateTime now = ZonedDateTime.now();
        tradeManager.getOpenTrades().values().forEach(trade -> {
            updateTradeProfitLoss(trade, finalTick, now);
            checkAndExecuteStopLossTakeProfit(trade, tradeManager, finalTick);
        });
    }

    @Override
    public void updateBalanceOnTradeClose(Trade trade, AccountManager accountManager) {
        double profit = trade.getProfit();
        double balance = accountManager.getBalance();
        double newBalance = balance + profit;
        accountManager.setBalance(newBalance);
        log.trace("Updating balance on trade close. Trade id: {}, Profit: {}, Balance: {}", trade.getId(), profit, newBalance);
    }

    @Override
    public void updateAccountEquityOnTick(AccountManager accountManager, TradeManager tradeManager) {
        updateAccountEquity(accountManager, tradeManager);

        // Risk management TODO: Should we move this out the trade manager?
        // We will stop running if we go below 10% of initial balance.
        if (accountManager.getEquity() < (accountManager.getInitialBalance() * 0.1)) {
            throw new RiskException("Strat ID: '" + strategyId + "'. Equity is below 10%. In order to prevent further losses, the strategy has been terminated.");
        }
    }

    private void updateTradeProfitLoss(Trade trade, Tick tick, ZonedDateTime now) {
        Number currentPrice = trade.isLong() ? tick.getBid() : tick.getAsk();
        Number priceDifference = trade.isLong()
                ? currentPrice.subtract(trade.getEntryPrice())
                : trade.getEntryPrice().subtract(currentPrice);

        double profit = priceDifference.getValue().doubleValue() * trade.getQuantity();
        trade.setProfit(profit);
        eventPublisher.publishEvent(new TradeEvent(this.strategyId, trade.getInstrument(), trade, TradeEvent.Action.UPDATE, now));
        log.trace("Updating trade profit/loss for trade id: {}. Profit: {}", trade.getId(), trade.getProfit());
    }

    private void checkAndExecuteStopLossTakeProfit(Trade trade, TradeManager tradeManager, Tick tick) {
        try {
            if (hasHitStopLoss(trade, tick)) {
                log.trace("Stop loss hit for trade id : {}. SL at: {}, current tick at: {}. Loss: {}", trade.getId(), trade.getStopLoss(), trade.isLong() ? tick.getBid() : tick.getAsk(), trade.getProfit());
                tradeManager.closePosition(trade.getId(), false);
            } else if (hasHitTakeProfit(trade, tick)) {
                log.trace("Take profit hit for trade id : {}. TP at: {}, current tick at: {}. Profit: {}", trade.getId(), trade.getTakeProfit(), trade.isLong() ? tick.getBid() : tick.getAsk(), trade.getProfit());
                tradeManager.closePosition(trade.getId(), false);
            }
        } catch (Exception e) {
            log.error("Error closing trade during stoploss/takeprofit calc: {}", trade, e);
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

    private void updateAccountEquity(AccountManager accountManager, TradeManager tradeManager) {
        // Calculate and set equity (balance + unrealized profit/loss from open trades)

        ConcurrentHashMap<Integer, Trade> openTrades = tradeManager.getOpenTrades();
        double unrealizedProfit = openTrades.isEmpty() ? 0.0 :
                openTrades.reduceValues(Long.MAX_VALUE, Trade::getProfit, Double::sum);

        double equity = accountManager.getBalance() + unrealizedProfit;
        accountManager.setEquity(equity);
        log.trace("Unrealized profit/loss: {}, Equity: {}", unrealizedProfit, equity);
        eventPublisher.publishEvent(new AccountEvent(strategyId, accountManager.getAccount()));
    }
}