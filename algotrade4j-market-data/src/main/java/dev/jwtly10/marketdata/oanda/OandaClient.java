package dev.jwtly10.marketdata.oanda;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.oanda.models.TradeStateFilter;
import dev.jwtly10.marketdata.oanda.response.OandaCandleResponse;
import dev.jwtly10.marketdata.oanda.response.OandaPriceResponse;
import dev.jwtly10.marketdata.oanda.response.OandaTradeResponse;
import dev.jwtly10.marketdata.oanda.utils.OandaUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Oanda API client
 * <a href="https://developer.oanda.com/rest-live-v20/introduction/">Official REST API Documentation</a>
 * Oanda deals with dates in New york time
 */
@Slf4j
public class OandaClient {
    private final String apiKey;
    private final String accountId;
    private final String apiUrl;
    private final OkHttpClient client;
    @Setter
    private String streamUrl = "https://stream-fxpractice.oanda.com";

    public OandaClient(String apiUrl, String apiKey, String accountId, OkHttpClient client) {
        this.apiKey = apiKey;
        this.accountId = accountId;
        this.apiUrl = apiUrl;
        this.client = client;
    }

    public OandaClient(String apiUrl, String apiKey, String accountId) {
        this(apiUrl, apiKey, accountId, new OkHttpClient());
    }

    /**
     * Streams prices for the specified instruments and invokes the callback for each price update.
     *
     * @param instruments the list of instruments to stream prices for
     * @param callback    the callback to be invoked for each price update
     */
    public void streamPrices(List<Instrument> instruments, PriceStreamCallback callback) {
        log.debug("Streaming prices for instruments: {}", instruments);
        String instrumentParams = instruments.stream()
                .map(Instrument::getOandaSymbol)
                .collect(Collectors.joining(","));

        String url = String.format("%s/v3/accounts/%s/pricing/stream?instruments=%s", streamUrl, accountId, instrumentParams);

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                        log.trace("New line from stream: {}", line);
                        if (line.contains("\"type\":\"PRICE\"")) {
                            ObjectMapper mapper = new ObjectMapper();
                            OandaPriceResponse priceResponse = mapper.readValue(line, OandaPriceResponse.class);
                            callback.onPrice(priceResponse);
                        }

                        if (line.contains("error")) {
                            throw new Exception("Error in stream: " + line);
                        }
                    }
                } catch (Exception e) {
                    callback.onError(e);
                } finally {
                    callback.onComplete();
                }
            }
        });
    }

    /**
     * Fetches candle data for the specified instrument and time period.
     *
     * @param instrument the instrument to fetch candles for
     * @param period     the duration of each candle
     * @param from       the start time for the data
     * @param to         the end time for the data
     * @return the OandaCandleResponse containing the candle data
     * @throws Exception if an error occurs while fetching the data
     */
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
     * Fetches trades from the Oanda API based on the specified parameters.
     *
     * @param ids        the list of trade ids to fetch
     * @param state      the state of the trades to fetch
     * @param instrument the instrument to filter by
     * @param count      the number of trades to fetch
     * @return the OandaTradeResponse containing the trade data
     * @throws Exception if an error occurs while fetching the data
     */
    public OandaTradeResponse fetchTrades(List<String> ids, TradeStateFilter state, Instrument instrument, Integer count) throws Exception {
        log.debug("Fetching trades for ids: {}, state: {}, instrument: {}, count: {}", ids, state, instrument, count);
        String url = buildTradesUrl(ids, state, instrument, count);

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String response = res.body().string();
            if (!res.isSuccessful()) {
                log.error("Failed to fetch trades from Oanda API: {}", res);
                throw new DataProviderException("Error response from Oanda API: " + response);
            }

            log.debug("Fetched trades: {}", response);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, OandaTradeResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch trades from Oanda API", e);
            throw e;
        }
    }

    /**
     * Builds the URL for fetching trades based on the specified parameters.
     *
     * @param ids        the list of trade ids to fetch
     * @param state      the state of the trades to fetch
     * @param instrument the instrument to filter by
     * @param count      the number of trades to fetch
     * @return the URL for fetching trades
     */
    private String buildTradesUrl(List<String> ids, TradeStateFilter state, Instrument instrument, Integer count) {
        // TODO: Haven't implemented beforeId (see Oanda API documentation)
        StringBuilder urlBuilder = new StringBuilder(String.format("%s/v3/accounts/%s/trades", apiUrl, accountId));

        List<String> queryParams = new ArrayList<>();

        if (ids != null && !ids.isEmpty()) {
            queryParams.add("ids=" + String.join(",", ids));
        }
        if (state != null) {
            queryParams.add("state=" + state.name());
        }
        if (instrument != null) {
            queryParams.add("instrument=" + instrument.getOandaSymbol());
        }
        if (count != null) {
            queryParams.add("count=" + count);
        }
        if (!queryParams.isEmpty()) {
            urlBuilder.append("?").append(String.join("&", queryParams));
        }
        return urlBuilder.toString();
    }

    /**
     * Callback interface for handling price stream updates.
     */
    public interface PriceStreamCallback {
        /**
         * Called when a new price is received.
         *
         * @param priceResponse the price response
         */
        void onPrice(OandaPriceResponse priceResponse);

        /**
         * Called when an error occurs during streaming.
         *
         * @param e the exception that occurred
         */
        void onError(Exception e);

        /**
         * Called when the streaming is complete.
         */
        void onComplete();
    }
}