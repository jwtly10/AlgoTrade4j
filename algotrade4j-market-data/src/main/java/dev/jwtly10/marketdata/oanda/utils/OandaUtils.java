package dev.jwtly10.marketdata.oanda.utils;

import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.marketdata.oanda.response.OandaCandleResponse;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

public class OandaUtils {
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