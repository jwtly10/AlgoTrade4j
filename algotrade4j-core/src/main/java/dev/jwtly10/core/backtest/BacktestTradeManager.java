package dev.jwtly10.core.backtest;

import dev.jwtly10.core.Number;
import dev.jwtly10.core.*;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.TradeEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BacktestTradeManager implements TradeManager {
    private final Map<String, Trade> trades;
    @Getter
    private final Account account;
    private final PriceFeed priceFeed;
    private final EventPublisher eventPublisher;
    private final String strategyId;

    public BacktestTradeManager(String strategyId, Number initialCash, PriceFeed priceFeed, EventPublisher eventPublisher) {
        this.strategyId = strategyId;
        this.account = new Account(initialCash);
        this.trades = new HashMap<>();
        this.priceFeed = priceFeed;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String openLongPosition(String symbol, Number quantity, Number stopLoss, Number takeProfit) {
        Number entryPrice = priceFeed.getAsk(symbol);
        Trade trade = new Trade(symbol, quantity, entryPrice, stopLoss, takeProfit, true);
        log.debug("Opening long position: {}", trade);

        trades.put(trade.getId(), trade);
        eventPublisher.publishEvent(new TradeEvent(strategyId, symbol, trade, TradeEvent.Action.OPEN));
        return trade.getId();
    }

    @Override
    public String openLongPosition(String symbol, Number stopLoss, Number riskRatio, Number risk, BALANCE_TYPE balanceType) {
        return openPosition(symbol, stopLoss, riskRatio, risk, balanceType, true);
    }

    @Override
    public String openShortPosition(String symbol, Number stopLoss, Number riskRatio, Number risk, BALANCE_TYPE balanceType) {
        return openPosition(symbol, stopLoss, riskRatio, risk, balanceType, false);
    }

    public String openPosition(String symbol, Number stopLoss, Number riskRatio, Number risk, BALANCE_TYPE balanceType, boolean isLong) {
        log.debug("Opening {} position for symbol: {}, stopLoss={}, riskRatio={}, risk={}, balanceType={}",
                isLong ? "long" : "short", symbol, stopLoss, riskRatio, risk, balanceType);
        Number entryPrice = isLong ? priceFeed.getAsk(symbol) : priceFeed.getBid(symbol);
        log.debug("Entry price ({}) for {}: {}", isLong ? "ask" : "bid", symbol, entryPrice);

        Number balance = switch (balanceType) {
            case INITIAL -> account.getInitialBalance();
            case BALANCE -> account.getBalance();
            case EQUITY -> account.getEquity();
        };

        log.debug("Selected balance ({} type): {}", balanceType, balance);

        Number riskInPercent = risk.divide(new Number("100").getValue());
        Number riskAmount = balance.multiply(riskInPercent.getValue());
        log.debug("Risk amount calculation: {} * {} = {}", balance, riskInPercent.getValue(), riskAmount);

        Number stopLossDistance = isLong ? entryPrice.subtract(stopLoss).abs() : stopLoss.subtract(entryPrice).abs();
        log.debug("Stop loss distance calculation: |{} - {}| = {}", isLong ? entryPrice : stopLoss, isLong ? stopLoss : entryPrice, stopLossDistance);

        Number quantity = riskAmount.divide(stopLossDistance.getValue());
        log.debug("Quantity calculation: {} / {} = {}", riskAmount, stopLossDistance.getValue(), quantity);

        Number takeProfit = isLong ?
                entryPrice.add(stopLossDistance.multiply(riskRatio.getValue())) :
                entryPrice.subtract(stopLossDistance.multiply(riskRatio.getValue()));
        log.debug("Take profit calculation: {} {} ({} * {}) = {}",
                entryPrice, isLong ? "+" : "-", stopLossDistance, riskRatio.getValue(), takeProfit);

        quantity = quantity.setScale(2, RoundingMode.DOWN);
        log.debug("Final quantity after rounding down to 2 decimal places: {}", quantity);

        log.debug("Opening {} position: symbol={}, entryPrice={}, stopLoss={}, takeProfit={}, quantity={}, riskAmount={}",
                isLong ? "long" : "short", symbol, entryPrice, stopLoss, takeProfit, quantity, riskAmount);

        Trade trade = new Trade(symbol, quantity, entryPrice, stopLoss, takeProfit, isLong);

        eventPublisher.publishEvent(new TradeEvent(strategyId, symbol, trade, TradeEvent.Action.OPEN));
        trades.put(trade.getId(), trade);

        return trade.getId();
    }

    @Override
    public String openShortPosition(String symbol, Number quantity, Number stopLoss, Number takeProfit) {
        Number entryPrice = priceFeed.getBid(symbol);
        Trade trade = new Trade(symbol, quantity, entryPrice, stopLoss, takeProfit, false);
        log.debug("Opening short position: {}", trade);

        trades.put(trade.getId(), trade);
        eventPublisher.publishEvent(new TradeEvent(strategyId, symbol, trade, TradeEvent.Action.OPEN));
        return trade.getId();
    }

    @Override
    public void closePosition(String tradeId) {
        Trade trade = trades.remove(tradeId);
        if (trade == null) {
            throw new IllegalArgumentException("Trade not found: " + tradeId);
        }

        if (priceFeed.getAsk(trade.getSymbol()) == null || priceFeed.getBid(trade.getSymbol()) == null) {
            throw new IllegalStateException("Price not found for symbol: " + trade.getSymbol());
        }

        log.debug("Closing position: {}", trade);
        log.debug("Trade details - Symbol: {}, Long: {}, Quantity: {}, Entry Price: {}",
                trade.getSymbol(), trade.isLong(), trade.getQuantity(), trade.getEntryPrice());

        Number closingPrice = trade.isLong() ? priceFeed.getBid(trade.getSymbol()) : priceFeed.getAsk(trade.getSymbol());
        log.debug("Closing price: {}", closingPrice);
        trade.setClosePrice(closingPrice);

        Number priceDifference;
        if (trade.isLong()) {
            priceDifference = closingPrice.subtract(trade.getEntryPrice());
            log.debug("Long trade - Price difference: {} - {} = {}",
                    closingPrice, trade.getEntryPrice(), priceDifference);
        } else {
            priceDifference = trade.getEntryPrice().subtract(closingPrice);
            log.debug("Short trade - Price difference: {} - {} = {}",
                    trade.getEntryPrice(), closingPrice, priceDifference);
        }

        Number profitLoss = priceDifference.multiply(trade.getQuantity().getValue());
        log.debug("Profit/Loss calculation: {} * {} = {}",
                priceDifference, trade.getQuantity().getValue(), profitLoss);

        Number newBalance = account.getBalance().add(profitLoss);
        log.debug("Updating balance: {} + {} = {}",
                account.getBalance(), profitLoss, newBalance);

        eventPublisher.publishEvent(new TradeEvent(strategyId, trade.getSymbol(), trade, TradeEvent.Action.CLOSE));
        account.setBalance(newBalance);
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
        String symbol = bar.getSymbol();
        Number bid = priceFeed.getBid(symbol);
        Number ask = priceFeed.getAsk(symbol);

        log.debug("Updating trades for symbol: {}, bid: {}, ask: {}, noTrades: {}", symbol, bid, ask, trades.size());

        List<String> tradesToClose = new ArrayList<>();

        for (Trade trade : trades.values()) {
            if (trade.getSymbol().equals(symbol)) {
                if (trade.isLong()) {
                    // For long positions, we compare against the bid price
                    if (bid.isLessThan(trade.getStopLoss()) || bid.isGreaterThan(trade.getTakeProfit())) {
                        tradesToClose.add(trade.getId());
                    }
                } else {
                    // For short positions, we compare against the ask price
                    if (ask.isGreaterThan(trade.getStopLoss()) || ask.isLessThan(trade.getTakeProfit())) {
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
        Number totalOpenPositionValue = Number.ZERO;

        for (Trade trade : trades.values()) {
            String symbol = trade.getSymbol();
            Number currentPrice = trade.isLong() ? priceFeed.getBid(symbol) : priceFeed.getAsk(symbol);
            Number positionValue = currentPrice.subtract(trade.getEntryPrice()).multiply(trade.getQuantity().getValue());

            if (!trade.isLong()) {
                positionValue = positionValue.multiply(new Number("-1").getValue());
            }

            totalOpenPositionValue = totalOpenPositionValue.add(positionValue);
        }

        Number newEquity = account.getBalance().add(totalOpenPositionValue);
        account.setEquity(newEquity);
        account.setOpenPositionValue(totalOpenPositionValue);
    }
}