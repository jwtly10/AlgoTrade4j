package dev.jwtly10.core.execution;

import dev.jwtly10.core.event.EventPublisher;
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
        log.debug("Opening {} position for instrument: {}, stopLoss={}, riskRatio={}, riskPercentage={}, balanceToRisk={}",
                isLong ? "long" : "short", params.getInstrument(), params.getStopLoss(), params.getRiskRatio(),
                params.getRiskPercentage(), params.getBalanceToRisk());

        Number entryPrice = params.getEntryPrice();
        if (!entryPrice.isEquals(currentTick.getAsk()) || !entryPrice.isEquals(currentTick.getBid())) {
            entryPrice = isLong ? currentTick.getAsk() : currentTick.getBid();
            log.warn("Entry price does not match current ask/bid price. Using current ask/bid price as entry price. (Wanted: {}, Got: {})", params.getEntryPrice(), entryPrice);
        }

        log.debug("Entry price for {}: {}", params.getInstrument(), entryPrice);

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

        if (params.getStopLoss().isLessThan(Number.ZERO) || takeProfit.isLessThan(Number.ZERO)) {
            String reason = params.getStopLoss().isLessThan(Number.ZERO) ? "STOP LOSS" : "TAKE PROFIT";
            log.warn("{} cannot be below 0", reason);
            throw new InvalidTradeException(String.format("%s cannot be below 0 for the new trade opened @ %s. Value was %s", reason, currentTick.getDateTime().toString(),
                    reason.equals("STOP LOSS") ? params.getStopLoss() : params.getTakeProfit()));
        }

        if (quantity.isLessThan(Number.ZERO)) {
            log.warn("Quantity cannot be below 0");
            throw new InvalidTradeException(String.format("Quantity cannot be below 0 for the new trade opened @ %s. Value was %s", currentTick.getDateTime().toString(), quantity));
        }

        Trade trade = new Trade(params.getInstrument(), quantity, entryPrice, barSeries.getLastBar().getOpenTime(),
                params.getStopLoss(), takeProfit, isLong);

        eventPublisher.publishEvent(new TradeEvent(strategyId, params.getInstrument(), trade, TradeEvent.Action.OPEN));
        allTrades.put(trade.getId(), trade);
        openTrades.put(trade.getId(), trade);

        log.info("Opened {} position: instrument={}, entryPrice={}, stopLoss={}, takeProfit={}, quantity={}, riskAmount={} at {}",
                trade.isLong() ? "long" : "short", trade.getInstrument(), trade.getEntryPrice(), trade.getStopLoss(), trade.getTakeProfit(), trade.getQuantity(), riskAmount, trade.getOpenTime());
        return trade.getId();
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

        log.debug("Closing position: {}", trade);
        log.debug("Trade details - Symbol: {}, Long: {}, Quantity: {}, Entry Price: {}",
                trade.getInstrument(), trade.isLong(), trade.getQuantity(), trade.getEntryPrice());


        var slippage = new SlippageModel();
        // TODO: We can support different volatility levels now. We can implement this if needed

        Number closingPrice = null;
        if (!manual) { // If not manual, this means this was triggered internally, potentially by stoploss/tp
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

        log.debug("Closing price: {}", closingPrice);
        trade.setClosePrice(closingPrice.setScale(2, RoundingMode.DOWN));
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

        trade.setProfit(profitLoss.roundMoneyDown());

        log.info("Trade {} closed at {} ({}) for {}", trade.getId(), trade.getClosePrice(), trade.getCloseTime(), trade.getProfit());

        eventPublisher.publishEvent(new TradeEvent(strategyId, trade.getInstrument(), trade, TradeEvent.Action.CLOSE));
        eventPublisher.publishEvent(new TradeEvent(strategyId, trade.getInstrument(), trade, TradeEvent.Action.UPDATE));
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
    public Number getOpenPositionValue(Instrument instrument) {
        return openTrades.values().stream()
                .filter(trade -> trade.getInstrument().equals(instrument))
                .map(Trade::getProfit)
                .reduce(Number.ZERO, Number::add);
    }
}