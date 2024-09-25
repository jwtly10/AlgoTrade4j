package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Trade;

import java.util.List;

public interface BrokerClient {
    Account getAccountInfo();

    List<Trade> getOpenTrades();

    List<Trade> getAllTrades() throws Exception;

    Trade openTrade(Trade trade);

    void closeTrade(Integer tradeId);

    void streamPrices(List<Instrument> instruments, Object callback);
}