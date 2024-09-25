package dev.jwtly10.liveapi.executor;

import dev.jwtly10.core.exception.InvalidTradeException;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.marketdata.common.BrokerClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LiveTradeManager implements TradeManager {

    private final BrokerClient brokerClient;

    private ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Trade> allTrades = new ConcurrentHashMap<>();

    private Tick currentTick;

    public LiveTradeManager(BrokerClient brokerClient) {
        this.brokerClient = brokerClient;
    }

    @Override
    public void updateOpenTrades(List<Trade> trades) {
        openTrades = new ConcurrentHashMap<>();
        trades.forEach(trade -> openTrades.put(trade.getId(), trade));
    }

    @Override
    public void updateAllTrades(List<Trade> trades) {
        allTrades = new ConcurrentHashMap<>();
        trades.forEach(trade -> allTrades.put(trade.getId(), trade));
    }

    @Override
    public Integer openLong(TradeParameters params) throws InvalidTradeException {
        params.setLong(true);
        return openPosition(params);
    }

    @Override
    public Integer openShort(TradeParameters params) throws InvalidTradeException {
        params.setLong(false);
        return openPosition(params);
    }

    private Integer openPosition(TradeParameters params) {
        return brokerClient.openTrade(params.createTrade()).getId();
    }

    @Override
    public void closePosition(Integer tradeId, boolean manual) throws InvalidTradeException {
        brokerClient.closeTrade(tradeId);
    }

    @Override
    public void loadTrades() throws Exception {
        List<Trade> trades = brokerClient.getAllTrades();
        trades.forEach(trade -> allTrades.put(trade.getId(), trade));

        trades.forEach(trade -> {
            if (trade.getClosePrice() == Number.ZERO) {
                openTrades.put(trade.getId(), trade);
            }
        });
    }

    @Override
    public Trade getTrade(Integer tradeId) {
        return allTrades.get(tradeId);
    }

    @Override
    public double getOpenPositionValue(Instrument instrument) {
        return openTrades.values().stream()
                .filter(trade -> trade.getInstrument().equals(instrument))
                .mapToDouble(trade -> trade.getQuantity() * currentTick.getBid().doubleValue())
                .sum();
    }

    @Override
    public Map<Integer, Trade> getAllTrades() {
        return allTrades;
    }

    @Override
    public ConcurrentHashMap<Integer, Trade> getOpenTrades() {
        return openTrades;
    }

    @Override
    public void setCurrentTick(Tick tick) {
        this.currentTick = tick;
    }
}