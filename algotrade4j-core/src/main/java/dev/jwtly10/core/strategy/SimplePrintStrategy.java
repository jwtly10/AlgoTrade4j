//package dev.jwtly10.core.strategy;
//
//import dev.jwtly10.core.Bar;
//import dev.jwtly10.core.Number;
//import dev.jwtly10.core.Tick;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class SimplePrintStrategy extends BaseStrategy {
//    public SimplePrintStrategy() {
//        super("SimplePrintStrategy");
//    }
//
//    @Override
//    public void onStart() {
//        log.info("Strategy initialized.");
//    }
//
//    @Override
//    public void onBarClose(Bar bar) {
//        log.info("New bar received: {}", formatBar(bar));
//
//        // Randomly decide to open a trade
//        if (Math.random() > 0.5) {
//            if (Math.random() > 0.5) {
//                if (priceFeed.getBid("NAS100_USD").isGreaterThan(new Number(14300))) {
//                    var tradeID = openShortPosition(bar.getSymbol(), new Number(10), new Number(17000), new Number(14000));
//                    log.info("Opened short position: {}", tradeID);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onTick(Tick tick, Bar currentBar) {
//
//    }
//
//    @Override
//    public void onDeInit() {
//        log.info("Strategy de-initialized.");
//    }
//
//    private String formatBar(Bar bar) {
//        return String.format("Time: %s, Open: %.2f, High: %.2f, Low: %.2f, Close: %.2f, Volume: %.2f",
//                bar.getOpenTime(),
//                bar.getOpen().doubleValue(),
//                bar.getHigh().doubleValue(),
//                bar.getLow().doubleValue(),
//                bar.getClose().doubleValue(),
//                (double) bar.getVolume());
//    }
//}