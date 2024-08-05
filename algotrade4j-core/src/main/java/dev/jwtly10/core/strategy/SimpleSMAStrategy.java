//package dev.jwtly10.core.strategy;
//
//
//import dev.jwtly10.core.Bar;
//import dev.jwtly10.core.Number;
//import dev.jwtly10.core.Tick;
//import dev.jwtly10.core.indicators.SMA;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class SimpleSMAStrategy extends BaseStrategy {
//    private SMA sma20;
//
//    public SimpleSMAStrategy() {
//        super("SimpleSMAStrategy");
//    }
//
//    @Override
//    protected void initIndicators() {
//        sma20 = createIndicator(SMA.class, 20);
//    }
//
//    @Override
//    public void onStart() {
//        log.info("SimpleSMAStrategy starting. Strategy ID: {}", getStrategyId());
////        log.info("Initial balance: {}", getBalance());
//    }
//
//    @Override
//    public void onTick(Tick tick, Bar currentBar) {
//        // Don't need to do anything on tick
//    }
//
//    @Override
//    public void onBarClose(Bar bar) {
//        String barInfo = String.format("New bar - Time: %s, Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f, Volume: %.2f",
//                bar.getOpenTime(),
//                bar.getOpen().doubleValue(),
//                bar.getHigh().doubleValue(),
//                bar.getLow().doubleValue(),
//                bar.getClose().doubleValue(),
//                (double) bar.getVolume());
//
//        log.info(barInfo);
//
//        if (sma20.isReady()) {
//            // If bearish candle below the sma sell
//            if (bar.getClose().isLessThan(sma20.getValue()) && bar.isBearish() && bar.getClose().isGreaterThan(new Number(13200))) {
//                openShortPosition(bar.getSymbol(), new Number(10), new Number(17000), new Number(13200));
//            }
//        }
//
//        log.info("Current balance: {}", getBalance());
//        log.info("--------------------");
//    }
//
//    @Override
//    public void onDeInit() {
//        log.info("SimpleSMAStrategy shutting down. Final balance: {}", getBalance());
//    }
//}