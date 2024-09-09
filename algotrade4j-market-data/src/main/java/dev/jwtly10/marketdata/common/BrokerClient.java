package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.Trade;
import dev.jwtly10.core.model.TradeParameters;

import java.util.List;

public interface BrokerClient {
    Account getAccountInfo();

    List<Trade> getOpenTrades();

    List<Trade> getAllTrades();

    Trade openTrade(TradeParameters params);

    void closeTrade(Integer tradeId);
}