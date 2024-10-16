package dev.jwtly10.core.execution;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.types.TradeEvent;
import dev.jwtly10.core.exception.InvalidTradeException;
import dev.jwtly10.core.exception.RiskManagerException;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.risk.RiskManager;
import dev.jwtly10.core.risk.RiskStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class BacktestTradeManager implements TradeManager {
    private final String strategyId;
    @Getter
    private final Map<Integer, Trade> allTrades;
    @Getter
    private final ConcurrentHashMap<Integer, Trade> openTrades;
    private final RiskManager riskManager;
    private final EventPublisher eventPublisher;
    private final BarSeries barSeries;
    private Consumer<Trade> onTradeCloseCallback;
    @Setter
    private Tick currentTick;

    private final Broker BROKER;

    public BacktestTradeManager(Broker broker, Tick currentTick, BarSeries barSeries, String strategyId, EventPublisher eventPublisher, RiskManager riskManager) {
        this.allTrades = new HashMap<>();
        this.openTrades = new ConcurrentHashMap<>();
        this.eventPublisher = eventPublisher;
        this.strategyId = strategyId;
        this.barSeries = barSeries;
        this.currentTick = currentTick;
        this.riskManager = riskManager;
        this.BROKER = broker;
    }

    @Override
    public Broker getBroker() {
        return BROKER;
    }

    @Override
    public void updateOpenTrades(List<Trade> trades) {

    }

    @Override
    public void updateAllTrades(List<Trade> trades) {

    }

    @Override
    public void setOnTradeCloseCallback(Consumer<Trade> callback) {
        this.onTradeCloseCallback = callback;
    }

    @Override
    public Trade openLong(TradeParameters params) {
        params.setLong(true);
        return openPosition(params);
    }

    @Override
    public Trade openShort(TradeParameters params) {
        params.setLong(false);
        return openPosition(params);
    }

    private Trade openPosition(TradeParameters params) {
        log.trace("Opening {} position for instrument: {}, stopLoss={}, riskRatio={}, riskPercentage={}, balanceToRisk={}",
                params.isLong() ? "long" : "short", params.getInstrument(), params.getStopLoss(), params.getRiskRatio(),
                params.getRiskPercentage(), params.getBalanceToRisk());

        RiskStatus risk = riskManager.canTrade();
        if (risk.isRiskViolated()) {
            throw new RiskManagerException(String.format("Can't open trade due to risk violation: %s", risk.getReason()));
        }

        Number entryPrice = params.getEntryPrice();
        if (!entryPrice.isEquals(currentTick.getAsk()) || !entryPrice.isEquals(currentTick.getBid())) {
            entryPrice = params.isLong() ? currentTick.getAsk() : currentTick.getBid();
            log.trace("Entry price does not match current ask/bid price. Using current ask/bid price as entry price. (Wanted: {}, Got: {})", params.getEntryPrice(), entryPrice);
        }

        // These values won't be known by the strategy specifically, we can inject them here
        params.setEntryPrice(entryPrice);
        params.setOpenTime(currentTick.getDateTime());

        log.trace("Entry price for {}: {}", params.getInstrument(), entryPrice);

        Trade trade = params.createTrade();

        eventPublisher.publishEvent(new TradeEvent(strategyId, params.getInstrument(), trade, TradeEvent.Action.OPEN));
        allTrades.put(trade.getId(), trade);
        openTrades.put(trade.getId(), trade);

        log.debug("Opened {} position @ {}: id={}, instrument={}, entryPrice={}, stopLoss={}, takeProfit={}, quantity={}",
                trade.isLong() ? "long" : "short", trade.getOpenTime(), trade.getId(), trade.getInstrument(), trade.getEntryPrice(), trade.getStopLoss(), trade.getTakeProfit(), trade.getQuantity());

        return trade;
    }

    @Override
    public void closePosition(Integer tradeId, boolean manual) throws InvalidTradeException {
        Trade trade = openTrades.remove(tradeId);
        if (trade == null) {
            throw new IllegalArgumentException("Trade not found: " + tradeId);
        }

        if (currentTick.getAsk() == null || currentTick.getBid() == null) {
            throw new IllegalStateException("Price not found for instrument: " + trade.getInstrument());
        }

        log.trace("Closing position: {}", trade);

        var slippage = new SlippageModel();
        // TODO: We can support different volatility levels now. We can implement this if needed

        Number closingPrice = null;
        if (!manual) { // If not manual, this means this was triggered internally, by stoploss/tp or strategy end (backtesting)
            closingPrice = slippage.calculateExecutionPrice(
                    trade.isLong(),
                    trade.getStopLoss(),
                    trade.getTakeProfit(),
                    currentTick.getAsk(),
                    currentTick.getBid(),
                    false);
        }

        // If we couldnt generate a good close price, just use the original implementation of current bid/tick
        if (closingPrice == null) {
            closingPrice = trade.isLong() ? currentTick.getBid() : currentTick.getAsk();
        }

        log.trace("Closing price: {}", closingPrice);
        trade.setClosePrice(closingPrice);
        trade.setCloseTime(currentTick.getDateTime());

        Number priceDifference;
        if (trade.isLong()) {
            priceDifference = closingPrice.subtract(trade.getEntryPrice());
            log.trace("Long trade - Price difference: {} - {} = {}",
                    closingPrice, trade.getEntryPrice(), priceDifference);
        } else {
            priceDifference = trade.getEntryPrice().subtract(closingPrice);
            log.trace("Short trade - Price difference: {} - {} = {}",
                    trade.getEntryPrice(), closingPrice, priceDifference);
        }

        double profitLoss = priceDifference.getValue().doubleValue() * trade.getQuantity();
        log.trace("Profit/Loss calculation: {} * {} = {}",
                priceDifference, trade.getQuantity(), profitLoss);

        trade.setProfit(profitLoss);

        log.debug("Trade {} closed at {} ({}) for {}", trade.getId(), trade.getClosePrice(), trade.getCloseTime(), trade.getProfit());

        eventPublisher.publishEvent(new TradeEvent(strategyId, trade.getInstrument(), trade, TradeEvent.Action.CLOSE));

        // Trigger any set callback on trade close
        if (onTradeCloseCallback != null) {
            onTradeCloseCallback.accept(trade);
        } else {
            log.warn("No callback set for trade close event");
        }
    }

    @Override
    public void loadTrades() {
        log.debug("Loading trades (During backtesting this is not needed)");
    }

    @Override
    public Trade getTrade(Integer tradeId) {
        return allTrades.get(tradeId);
    }

    @Override
    public double getOpenPositionValue(Instrument instrument) {
        return openTrades.values().stream()
                .filter(trade -> trade.getInstrument().equals(instrument))
                .map(Trade::getProfit)
                .reduce(0.0, Double::sum);
    }

    @Override
    public void shutdown() {
        // Not needed for backtesting
    }

    @Override
    public void start() {
        // Not needed for backtesting
    }
}