package dev.jwtly10.liveapi.executor;

import dev.jwtly10.core.data.DataManager;
import dev.jwtly10.core.exception.RiskManagerException;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.risk.RiskManager;
import dev.jwtly10.core.risk.RiskStatus;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.common.TradeDTO;
import dev.jwtly10.marketdata.common.stream.Stream;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class LiveTradeManager implements TradeManager {

    private final ExecutorService executorService;
    private final BrokerClient brokerClient;
    private final RiskManager riskManager;
    private DataManager dataManager = null;
    private Stream<List<TradeDTO>> transactionStream;
    private Consumer<Trade> onTradeCloseCallback;

    private ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Trade> allTrades = new ConcurrentHashMap<>();

    private Tick currentTick;

    public LiveTradeManager(BrokerClient brokerClient, RiskManager riskManager) {
        this.brokerClient = brokerClient;
        this.riskManager = riskManager;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void start() {
        // Start the transaction stream
        this.transactionStream = (Stream<List<TradeDTO>>) brokerClient.streamTransactions();
        transactionStream.start(new Stream.StreamCallback<>() {
            @Override
            public void onData(List<TradeDTO> closedTradeDTOs) {
                closedTradeDTOs.forEach(t -> {
                    Trade trade = allTrades.get(Integer.parseInt(t.tradeId()));
                    if (trade != null) {
                        trade.setClosePrice(new Number(t.closePrice()));
                        trade.setCloseTime(ZonedDateTime.now());
                        trade.setProfit(t.profit());
                        allTrades.put(trade.getId(), trade);
                        openTrades.remove(trade.getId());
                        // Trigger any set callback on trade close
                        if (onTradeCloseCallback != null) {
                            onTradeCloseCallback.accept(trade);
                        } else {
                            log.warn("No callback set for trade close event");
                        }
                    } else {
                        log.warn("Trade not found for order fill transaction: {}", t.tradeId());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                log.error("Error in transaction stream", e);
            }

            @Override
            public void onComplete() {
                log.info("Transaction stream completed");
            }
        });

    }

    @Override
    public Broker getBroker() {
        return brokerClient.getBroker();
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
    public void setOnTradeCloseCallback(Consumer<Trade> callback) {
        this.onTradeCloseCallback = callback;
    }

    @Override
    public Trade openLong(TradeParameters params) throws Exception {
        params.setLong(true);
        try {
            return openPosition(params);
        } catch (Exception e) {
            log.error("Error opening long trade", e);
            throw e;
        }
    }

    @Override
    public Trade openShort(TradeParameters params) throws Exception {
        params.setLong(false);
        try {
            return openPosition(params);
        } catch (Exception e) {
            log.error("Error opening short trade", e);
            throw e;
        }
    }

    private Trade openPosition(TradeParameters params) throws Exception {
        RiskStatus risk = riskManager.canTrade();
        if (risk.isRiskViolated()) {
            throw new RiskManagerException(String.format("Can't open trade due to risk violation: %s", risk.getReason()));
        }

        Trade trade = brokerClient.openTrade(params);

        log.info("Opened {} position @ {}: id={}, instrument={}, entryPrice={}, stopLoss={}, takeProfit={}, quantity={}",
                trade.isLong() ? "long" : "short", trade.getOpenTime(), trade.getId(), trade.getInstrument(), trade.getEntryPrice(), trade.getStopLoss(), trade.getTakeProfit(), trade.getQuantity());

        return trade;
    }

    @Override
    public void closePosition(Integer tradeId, boolean manual) throws Exception {
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

    @Override
    public void shutdown() {
        log.info("Shutting down transaction streams.");
        if (dataManager != null) {
            dataManager.stop();
        }

        if (transactionStream != null) {
            transactionStream.close();
        }

        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    @Override
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }
}