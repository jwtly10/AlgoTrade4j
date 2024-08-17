package dev.jwtly10.marketdata.oanda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.instruments.OandaInstrument;
import dev.jwtly10.core.model.DefaultBar;
import dev.jwtly10.core.model.Number;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.Duration;
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
    private final String baseUrl;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public OandaClient(String apiKey, String accountId, String baseUrl, OkHttpClient client, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.accountId = accountId;
        this.baseUrl = baseUrl;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public OandaClient(String apiKey, String accountId, String baseUrl) {
        this(apiKey, accountId, baseUrl, new OkHttpClient(), new ObjectMapper());
    }

    public List<DefaultBar> fetchBars(OandaInstrument instrument, ZonedDateTime from, ZonedDateTime to, Duration period) {
        log.debug("Fetching data for instrument: {}, from: {}, to: {}, period: {}", instrument, from, to, period);
        String endpoint = String.format("/v3/instruments/%s/candles", instrument);
        String url = baseUrl + endpoint;

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        String fromParam = from.format(formatter);
        String toParam = to.format(formatter);
        String granularity = convertPeriodToGranularity(period);

        Request req = new Request.Builder()
                .url(url + String.format("?includeFirst=false&from=%s&to=%s&granularity=%s", fromParam, toParam, granularity))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept-Datetime-Format", "RFC3339")
                .build();

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                log.error("Failed to fetch data from Oanda API: {}", res.body().string());
                return List.of();
            }

            String response = res.body().string();
            log.debug("Response: {}", response);

            OandaCandleResponse candleResponse = objectMapper.readValue(response, OandaCandleResponse.class);
            return convertOandaCandles(candleResponse);
        } catch (Exception e) {
            log.error("Failed to fetch data from Oanda API", e);
            return List.of();
        }
    }

    /**
     * Convert Oanda candles to a list of DefaultBar
     *
     * @param res the Oanda candle response
     * @return a list of DefaultBar
     */
    private List<DefaultBar> convertOandaCandles(OandaCandleResponse res) {
        return res.candles().stream()
                .map(candle -> new DefaultBar(res.instrument(),
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