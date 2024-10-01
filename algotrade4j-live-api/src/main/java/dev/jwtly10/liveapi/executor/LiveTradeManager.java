package dev.jwtly10.liveapi.executor;

import dev.jwtly10.core.exception.InvalidTradeException;
import dev.jwtly10.core.exception.RiskManagerException;
import dev.jwtly10.core.execution.TradeManager;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.core.risk.RiskManager;
import dev.jwtly10.core.risk.RiskStatus;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import dev.jwtly10.marketdata.oanda.response.transaction.OrderFillTransaction;
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
public class LiveTradeManager implements TradeManager, AutoCloseable {

    private final ExecutorService executorService;

    private final BrokerClient brokerClient;

    private final RiskManager riskManager;

    private ConcurrentHashMap<Integer, Trade> openTrades = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Trade> allTrades = new ConcurrentHashMap<>();
    private Consumer<Trade> onTradeCloseCallback;

    private Tick currentTick;

    private boolean running;

    public LiveTradeManager(BrokerClient brokerClient, RiskManager riskManager) {
        this.brokerClient = brokerClient;
        this.riskManager = riskManager;
        this.running = true;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void start() {
        // Start the transaction stream
        startTransactionStream();
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
        RiskStatus risk = riskManager.canTrade();
        if (risk.isRiskViolated()) {
            throw new RiskManagerException(String.format("Can't open trade due to risk violation: %s", risk.getReason()));
        }

        Trade trade = brokerClient.openTrade(params.createTrade());

        log.info("Opened {} position @ {}: id={}, instrument={}, entryPrice={}, stopLoss={}, takeProfit={}, quantity={}",
                trade.isLong() ? "long" : "short", trade.getOpenTime(), trade.getId(), trade.getInstrument(), trade.getEntryPrice(), trade.getStopLoss(), trade.getTakeProfit(), trade.getQuantity());

        return trade.getId();
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

    @Override
    public void shutdown() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }

    private void startTransactionStream() {
        executorService.submit(() -> {
            while (running) {
                try {
                    brokerClient.streamTransactions(new OandaClient.TransactionStreamCallback() {
                        @Override
                        public void onOrderFillMarketClose(OrderFillTransaction transaction) {
                            // TODO: This should be refactored to support multiple different providers.
                            // Or we just have a live manager per implementation (which I think may be better)

                            // An oanda transaction can contain multiple trade closes.
                            // So we iterate over all of them and run the callback for each.
                            // Generally speaking there will only be one, but this is just to handle the edge case.
                            transaction.tradesClosed().forEach(tradeClose -> {
                                Trade trade = allTrades.get(Integer.parseInt(tradeClose.tradeID()));
                                if (trade != null) {
                                    trade.setClosePrice(new Number(tradeClose.price()));
                                    trade.setCloseTime(ZonedDateTime.parse(transaction.time()));
                                    trade.setProfit(Double.parseDouble(tradeClose.realizedPL()));
                                    allTrades.put(trade.getId(), trade);
                                    openTrades.remove(trade.getId());
                                    // Trigger any set callback on trade close
                                    if (onTradeCloseCallback != null) {
                                        onTradeCloseCallback.accept(trade);
                                    } else {
                                        log.warn("No callback set for trade close event");
                                    }
                                } else {
                                    log.warn("Trade not found for order fill transaction: {}", tradeClose);
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
                            if (running) {
                                log.info("Restarting transaction stream...");
                            }
                        }
                    });
                } catch (Exception e) {
                    log.error("Error starting transaction stream", e);
                    if (running) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        });
    }
}