package dev.jwtly10.marketdata.oanda;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.oanda.models.OandaOrder;
import dev.jwtly10.marketdata.oanda.models.TradeStateFilter;
import dev.jwtly10.marketdata.oanda.request.MarketOrderRequest;
import dev.jwtly10.marketdata.oanda.response.OandaAccountResponse;
import dev.jwtly10.marketdata.oanda.response.OandaCandleResponse;
import dev.jwtly10.marketdata.oanda.response.OandaOpenTradeResponse;
import dev.jwtly10.marketdata.oanda.response.OandaTradeResponse;
import dev.jwtly10.marketdata.oanda.stream.OandaPriceStream;
import dev.jwtly10.marketdata.oanda.stream.OandaTransactionStream;
import dev.jwtly10.marketdata.oanda.utils.OandaUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Oanda API client
 * <a href="https://developer.oanda.com/rest-live-v20/introduction/">Official REST API Documentation</a>
 * Oanda deals with dates in New york time
 */
@Slf4j
public class OandaClient {
    /**
     * Map of active price streams.
     */
    private final Map<String, Call> activePriceStreams = new ConcurrentHashMap<>();
    /**
     * Map of active transaction streams.
     */
    private final Map<String, Call> activeTransactionStreams = new ConcurrentHashMap<>();

    private final String apiKey;
    private final String apiUrl;
    private final OkHttpClient client;
    private final String streamUrl;
    private final ObjectMapper objectMapper;

    public OandaClient(String apiUrl, String apiKey, OkHttpClient client, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.client = client;
        this.objectMapper = objectMapper;

        if (apiUrl.contains("trade")) {
            // TODO LIVE TRADING NEED TO IMPL THIS.
//            streamUrl = "https://stream-fxtrade.oanda.com";
            throw new IllegalArgumentException("Live trading on real account is not supported yet");
        } else {
            streamUrl = "https://stream-fxpractice.oanda.com";
        }
    }

    public OandaClient(String apiUrl, String apiKey, ObjectMapper objectMapper) {
        this(apiUrl, apiKey, new OkHttpClient(), objectMapper);
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

            return objectMapper.readValue(response, OandaCandleResponse.class);
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
    public OandaTradeResponse fetchTrades(String accountId, List<String> ids, TradeStateFilter state, Instrument instrument, Integer count) throws Exception {
        log.trace("Fetching trades for ids: {}, state: {}, instrument: {}, count: {}", ids, state, instrument, count);
        String url = buildTradesUrl(accountId, ids, state, instrument, count);

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String response = res.body().string();
            if (!res.isSuccessful()) {
                throw new DataProviderException("Error response from Oanda API: " + response);
            }

            log.trace("Fetched trades: {}", response);

            return objectMapper.readValue(response, OandaTradeResponse.class);
        }
    }

    /**
     * Fetches the account details for the specified account.
     *
     * @return the OandaAccountResponse containing the account details
     * @throws Exception if an error occurs while fetching the data
     */
    public OandaAccountResponse fetchAccount(String accountId) throws Exception {
        log.trace("Fetching account details for account {}", accountId);

        String url = apiUrl + "/v3/accounts/" + accountId;

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String response = res.body().string();
            if (!res.isSuccessful()) {
                throw new DataProviderException("Error response from Oanda API: " + response);
            }

            log.trace("Fetched account details: {}", response);
            return objectMapper.readValue(response, OandaAccountResponse.class);
        }
    }

    /**
     * Opens a trade based on the specified order request.
     *
     * @param accountId    the account id to open the trade for
     * @param orderRequest the order request to open the trade with
     * @return the OandaOpenTradeResponse containing the trade details
     * @throws Exception if an error occurs while opening the trade
     */
    public OandaOpenTradeResponse openTrade(String accountId, MarketOrderRequest orderRequest) throws Exception {
        log.trace("Opening trade: {}", orderRequest);
        String url = apiUrl + "/v3/accounts/" + accountId + "/orders";

        OandaOrder order = new OandaOrder(orderRequest);

        String reqJson = objectMapper.writeValueAsString(order);

        log.trace("Request JSON: {}", reqJson);

        RequestBody body = RequestBody.create(reqJson, MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String response = res.body().string();
            if (!res.isSuccessful()) {
                throw new DataProviderException("Error response from Oanda API: " + response);
            }

            log.trace("Opened trade: {}", response);
            return objectMapper.readValue(response, OandaOpenTradeResponse.class);
        }
    }

    /**
     * Closes a trade based on the specified trade id.
     *
     * @param accountId the account id to close the trade for
     * @param id        the trade id to close
     * @throws Exception if an error occurs while closing the trade
     */

    public void closeTrade(String accountId, String id) throws Exception {
        log.trace("Closing trade: {}", id);
        String url = apiUrl + "/v3/accounts/" + accountId + "/trades/" + id + "/close";

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .put(RequestBody.create("", MediaType.parse("application/json")))
                .build();

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                throw new DataProviderException("Error response from Oanda API: " + res.body().string());
            }

            log.trace("Closed trade: {}", id);
        }
    }

    // Streaming endpoints

    /**
     * Returns OandaPriceStream runnable object to stream prices for the specified instruments.
     *
     * @param instruments the list of instruments to stream prices for
     */
    public OandaPriceStream streamPrices(String accountId, List<Instrument> instruments) {
        return new OandaPriceStream(client, apiKey, streamUrl, accountId, instruments, objectMapper);
    }

    /**
     * Returns OandaTransactionStream runnable object to stream transactions for the specified account.
     *
     * @param accountId the account id to stream transactions for
     */
    public OandaTransactionStream streamTransactions(String accountId) {
        return new OandaTransactionStream(client, apiKey, streamUrl, accountId, objectMapper);
    }

    // Utils

    /**
     * Builds the URL for fetching trades based on the specified parameters.
     *
     * @param accountId  the account id to build url for
     * @param ids        the list of trade ids to fetch
     * @param state      the state of the trades to fetch
     * @param instrument the instrument to filter by
     * @param count      the number of trades to fetch
     * @return the URL for fetching trades
     */
    private String buildTradesUrl(String accountId, List<String> ids, TradeStateFilter state, Instrument instrument, Integer count) {
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
}