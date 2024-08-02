package dev.jwtly10.core;

import java.util.List;

public interface Strategy {
    /**
     * Called once before the processing starts.
     */
    void onInit(BarSeries series, List<Indicator> indicators, TradeExecutor tradeExecutor);

    /**
     * Called on each bar.
     */
    void onBar(Bar bar, BarSeries series, List<Indicator> indicators, TradeExecutor tradeExecutor);

    /**
     * Called on each tick.
     * NOT IMPLEMENTED YET - DO NOT USE
     */
    void onTick();

    /**
     * Called once after the processing ends.
     */
    void onDeInit();
}