package dev.jwtly10.marketdata.impl.oanda.utils;

import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.DefaultTick;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.marketdata.impl.oanda.models.PriceBucket;
import dev.jwtly10.marketdata.impl.oanda.response.OandaCandleResponse;
import dev.jwtly10.marketdata.impl.oanda.response.OandaPriceResponse;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;

public class OandaUtils {
    private static final DateTimeFormatter OANDA_DT_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .appendOffset("+HH:MM", "Z")
            .toFormatter();

    /**
     * Maps an Oanda price response to a DefaultTick object.
     *
     * @param oandaPrice the Oanda price response
     * @return the mapped DefaultTick object
     */
    public static DefaultTick mapPriceToTick(OandaPriceResponse oandaPrice) {
        Instrument instrument = Instrument.fromOandaSymbol(oandaPrice.instrument());
        ZonedDateTime timestamp = ZonedDateTime.parse(oandaPrice.time(), OANDA_DT_FORMATTER);

        Number bidPrice = getBestPrice(oandaPrice.bids());
        Number askPrice = getBestPrice(oandaPrice.asks());
        Number midPrice = calculateMidPrice(bidPrice, askPrice);

        // TODO: Calculate volume
        return new DefaultTick(instrument, bidPrice, midPrice, askPrice, Number.ZERO, timestamp);
    }

    /**
     * Gets the best price from a list of price buckets.
     *
     * @param priceBuckets the list of price buckets
     * @return the best price as a Number object
     */
    private static Number getBestPrice(List<PriceBucket> priceBuckets) {
        if (priceBuckets == null || priceBuckets.isEmpty()) {
            return null;
        }
        return new Number(priceBuckets.getFirst().price());
    }

    /**
     * Calculates the mid price from bid and ask prices.
     *
     * @param bid the bid price
     * @param ask the ask price
     * @return the mid price as a Number object
     */
    private static Number calculateMidPrice(Number bid, Number ask) {
        if (bid == null || ask == null) {
            return null;
        }
        return bid.add(ask).divide(2);
    }

    /**
     * Convert Oanda candles to a list of DefaultBar
     *
     * @param res the Oanda candle response
     * @return a list of DefaultBar
     */
    public static List<DefaultBar> convertOandaCandles(OandaCandleResponse res) {
        var instrument = Instrument.fromOandaSymbol(res.instrument());
        return res.candles().stream()
                .map(candle -> new DefaultBar(instrument,
                        convertGranularityToDuration(res.granularity()),
                        ZonedDateTime.parse(candle.time()),
                        new Number(candle.mid().o()),
                        new Number(candle.mid().h()),
                        new Number(candle.mid().l()),
                        new Number(candle.mid().c()),
                        new Number(candle.volume())))
                .toList();
    }

    /**
     * Convert a period to a granularity string
     *
     * @param period the period
     * @return the granularity string in for the format accepted by the Oanda API
     */
    public static String convertPeriodToGranularity(Duration period) {
        if (period.toMinutes() == 1) return "M1";
        if (period.toMinutes() == 5) return "M5";
        if (period.toMinutes() == 15) return "M15";
        if (period.toMinutes() == 30) return "M30";
        if (period.toHours() == 1) return "H1";
        if (period.toHours() == 4) return "H4";
        if (period.toDays() == 1) return "D";
        throw new IllegalArgumentException("Unsupported period: " + period);
    }

    /**
     * Convert a granularity string to a duration
     *
     * @param granularity the granularity string
     * @return the duration
     */
    public static Duration convertGranularityToDuration(String granularity) {
        return switch (granularity) {
            case "M1" -> Duration.ofMinutes(1);
            case "M5" -> Duration.ofMinutes(5);
            case "M15" -> Duration.ofMinutes(15);
            case "M30" -> Duration.ofMinutes(30);
            case "H1" -> Duration.ofHours(1);
            case "H4" -> Duration.ofHours(4);
            case "D" -> Duration.ofDays(1);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        };
    }

}