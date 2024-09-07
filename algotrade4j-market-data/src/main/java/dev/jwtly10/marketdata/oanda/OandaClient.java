package dev.jwtly10.marketdata.oanda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Number;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Oanda API client
 * https://developer.oanda.com/rest-live-v20/introduction/
 * Oanda deals with dates in New york time
 */
@Slf4j
public class OandaClient {
    private final String apiKey;
    private final String accountId;
    private final String apiUrl;
    private final OkHttpClient client;

    public OandaClient(String apiUrl, String apiKey, String accountId, OkHttpClient client) {
        this.apiKey = apiKey;
        this.accountId = accountId;
        this.apiUrl = apiUrl;
        this.client = client;
    }

    public OandaClient(String apiUrl, String apiKey, String accountId) {
        this(apiUrl, apiKey, accountId, new OkHttpClient());
    }

    public InstrumentCandlesRequest instrumentCandles(Instrument instrument) {
        return new InstrumentCandlesRequest(this, instrument);
    }


    public OandaCandleResponse fetchCandles(Instrument instrument, Duration period, ZonedDateTime from, ZonedDateTime to) throws Exception {
        log.debug("Fetching candles for : {} ({}) time: {} -> {}",
                instrument,
                period,
                from.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                to.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        String endpoint = String.format("/v3/instruments/%s/candles", instrument.getOandaSymbol());
        String url = apiUrl + endpoint;

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        String fromParam = from.format(formatter);
        String toParam = to.format(formatter);
        String granularity = OandaUtils.convertPeriodToGranularity(period);

        Request req = new Request.Builder()
                .url(url + String.format("?includeFirst=false&from=%s&to=%s&granularity=%s", fromParam, toParam, granularity))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept-Datetime-Format", "RFC3339")
                .build();

        try (Response res = client.newCall(req).execute()) {
            String response = res.body().string();
            if (!res.isSuccessful()) {
                log.error("Failed to fetch data from Oanda API: {}", res);
                throw new DataProviderException("Error response from Oanda API: " + response);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, OandaCandleResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch data from Oanda API", e);
            throw e;
        }
    }

    /**
     * Convert Oanda candles to a list of DefaultBar
     *
     * @param res the Oanda candle response
     * @return a list of DefaultBar
     */
    private List<DefaultBar> convertOandaCandles(OandaCandleResponse res) {
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
    private String convertPeriodToGranularity(Duration period) {
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
    private Duration convertGranularityToDuration(String granularity) {
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

    // Inner classes for JSON deserialization
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OandaCandleResponse(List<OandaCandle> candles, String instrument, String granularity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OandaCandle(String time, OandaCandleMid mid, int volume) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OandaCandleMid(String o, String h, String l, String c) {
    }
}