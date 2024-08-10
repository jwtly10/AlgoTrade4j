package dev.jwtly10.core.execution;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.event.LogEvent;
import dev.jwtly10.core.event.TradeEvent;
import dev.jwtly10.core.exception.InvalidTradeException;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultTradeManager implements TradeManager {
    private final String strategyId;
    @Getter
    private final Map<Integer, Trade> allTrades;
    @Getter
    private final ConcurrentHashMap<Integer, Trade> openTrades;
    private final EventPublisher eventPublisher;
    private final BarSeries barSeries;
    @Setter
    private Tick currentTick;

    public DefaultTradeManager(Tick currentTick, BarSeries barSeries, String strategyId, EventPublisher eventPublisher) {
        this.allTrades = new HashMap<>();
        this.openTrades = new ConcurrentHashMap<>();
        this.eventPublisher = eventPublisher;
        this.strategyId = strategyId;
        this.barSeries = barSeries;
        this.currentTick = currentTick;
    }

    @Override
    public Integer openLong(TradeParameters params) {
        return openPosition(params, true);
    }

    @Override
    public Integer openShort(TradeParameters params) {
        return openPosition(params, false);
    }

    private Integer openPosition(TradeParameters params, boolean isLong) {
        log.debug("Opening {} position for symbol: {}, stopLoss={}, riskRatio={}, riskPercentage={}, balanceToRisk={}",
                isLong ? "long" : "short", params.getSymbol(), params.getStopLoss(), params.getRiskRatio(),
                params.getRiskPercentage(), params.getBalanceToRisk());

        Number entryPrice = params.getEntryPrice();
        log.debug("Entry price for {}: {}", params.getSymbol(), entryPrice);

        Number balance = params.getBalanceToRisk();
        log.debug("Selected balance: {}", balance);

        Number riskInPercent = params.getRiskPercentage().divide(new Number("100").getValue());
        Number riskAmount = balance.multiply(riskInPercent.getValue());
        log.debug("Risk amount calculation: {} * {} = {}", balance, riskInPercent.getValue(), riskAmount);

        Number stopLossDistance = isLong ? entryPrice.subtract(params.getStopLoss()).abs() :
                params.getStopLoss().subtract(entryPrice).abs();
        log.debug("Stop loss distance calculation: |{} - {}| = {}",
                isLong ? entryPrice : params.getStopLoss(),
                isLong ? params.getStopLoss() : entryPrice, stopLossDistance);

        Number quantity = params.getQuantity() != null ? params.getQuantity() :
                riskAmount.divide(stopLossDistance.getValue());
        log.debug("Quantity calculation: {} / {} = {}", riskAmount, stopLossDistance.getValue(), quantity);

        Number takeProfit = params.getTakeProfit() != null ? params.getTakeProfit() :
                (isLong ? entryPrice.add(stopLossDistance.multiply(params.getRiskRatio().getValue())) :
                        entryPrice.subtract(stopLossDistance.multiply(params.getRiskRatio().getValue())));
        log.debug("Take profit calculation: {} {} ({} * {}) = {}",
                entryPrice, isLong ? "+" : "-", stopLossDistance, params.getRiskRatio().getValue(), takeProfit);

        quantity = quantity.setScale(2, RoundingMode.DOWN);
        log.debug("Final quantity after rounding down to 2 decimal places: {}", quantity);

        log.debug("Opening {} position: symbol={}, entryPrice={}, stopLoss={}, takeProfit={}, quantity={}, riskAmount={}",
                isLong ? "long" : "short", params.getSymbol(), entryPrice, params.getStopLoss(), takeProfit, quantity, riskAmount);

        Trade trade = new Trade(params.getSymbol(), quantity, entryPrice, barSeries.getLastBar().getOpenTime(),
                params.getStopLoss(), takeProfit, isLong);

        eventPublisher.publishEvent(new TradeEvent(strategyId, params.getSymbol(), trade, TradeEvent.Action.OPEN));
        eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Opening " + (isLong ? "long" : "short") + " position for " + params.getSymbol()));
        allTrades.put(trade.getId(), trade);
        openTrades.put(trade.getId(), trade);
        return trade.getId();
    }

    @Override
    public void closePosition(Integer tradeId) throws InvalidTradeException {
        Trade trade = openTrades.remove(tradeId);
        if (trade == null) {
            throw new IllegalArgumentException("Trade not found: " + tradeId);
        }

        if (currentTick.getAsk() == null || currentTick.getBid() == null) {
            throw new IllegalStateException("Price not found for symbol: " + trade.getSymbol());
        }

        // TODO: Noticed potential bug with this. REview
        log.debug("Closing position: {}", trade);
        log.debug("Trade details - Symbol: {}, Long: {}, Quantity: {}, Entry Price: {}",
                trade.getSymbol(), trade.isLong(), trade.getQuantity(), trade.getEntryPrice());

        Number closingPrice = trade.isLong() ? currentTick.getBid() : currentTick.getAsk();
        log.debug("Closing price: {}", closingPrice);
        trade.setClosePrice(closingPrice);
        trade.setCloseTime(currentTick.getDateTime());

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

        trade.setProfit(profitLoss);

        eventPublisher.publishEvent(new TradeEvent(strategyId, trade.getSymbol(), trade, TradeEvent.Action.CLOSE));
        eventPublisher.publishEvent(new TradeEvent(strategyId, trade.getSymbol(), trade, TradeEvent.Action.UPDATE));
        eventPublisher.publishEvent(new LogEvent(strategyId, LogEvent.LogType.INFO, "Closing position {} for {}", trade.getId(), trade.getSymbol()));
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
    public Number getOpenPositionValue(String symbol) {
        return openTrades.values().stream()
                .filter(trade -> trade.getSymbol().equals(symbol))
                .map(Trade::getProfit)
                .reduce(Number.ZERO, Number::add);
    }
}