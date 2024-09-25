package dev.jwtly10.core.event;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.analysis.LiveAnalysisStats;
import dev.jwtly10.core.analysis.PerformanceAnalyser;
import dev.jwtly10.core.model.Instrument;
import lombok.Getter;

@Getter
public class LiveAnalysisEvent extends BaseEvent {
    private final LiveAnalysisStats stats;

    public LiveAnalysisEvent(String strategyId, Instrument instrument, PerformanceAnalyser performanceAnalyser, Account account) {
        super(strategyId, "LIVE_ANALYSIS", instrument);
        this.stats = new LiveAnalysisStats(
                account.getBalance(),
                account.getEquity(),
                performanceAnalyser.getOpenTradeProfit(),
                performanceAnalyser.getProfitFactor(),
                performanceAnalyser.getSharpeRatio(),
                performanceAnalyser.getTotalTradeInclOpen(),
                (performanceAnalyser.getLongWinPercentage() + performanceAnalyser.getShortWinPercentage()) / 2
        );
    }
}