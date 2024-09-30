package dev.jwtly10.marketdata.oanda;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jwtly10.core.exception.DataProviderException;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.marketdata.oanda.models.OandaOrder;
import dev.jwtly10.marketdata.oanda.models.TradeStateFilter;
import dev.jwtly10.marketdata.oanda.request.MarketOrderRequest;
import dev.jwtly10.marketdata.oanda.response.*;
import dev.jwtly10.marketdata.oanda.response.transaction.OrderFillTransaction;
import dev.jwtly10.marketdata.oanda.utils.OandaUtils;
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
    /**
     * ObjectMapper instance for JSON processing.
     */
    protected static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final String apiKey;
    private final String apiUrl;
    private final OkHttpClient client;
    private final String streamUrl;

    public OandaClient(String apiUrl, String apiKey, OkHttpClient client) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.client = client;

        if (apiUrl.contains("trade")) {
//            streamUrl = "https://stream-fxtrade.oanda.com";
            throw new IllegalArgumentException("Live trading on real account is not supported yet");
        } else {
            streamUrl = "https://stream-fxpractice.oanda.com";
        }
    }

    public OandaClient(String apiUrl, String apiKey) {
        this(apiUrl, apiKey, new OkHttpClient());
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
                log.error("Failed to fetch trades from Oanda API: {}", res);
                throw new DataProviderException("Error response from Oanda API: " + response);
            }

            log.trace("Fetched trades: {}", response);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, OandaTradeResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch trades from Oanda API", e);
            throw e;
        }
    }

    /**
     * Fetches the account details for the specified account.
     *
     * @return the OandaAccountResponse containing the account details
     * @throws Exception
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
                log.error("Failed to fetch Account details from Oanda API: {}", res);
                throw new DataProviderException("Error response from Oanda API: " + response);
            }

            log.trace("Fetched account details: {}", response);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response, OandaAccountResponse.class);
        } catch (Exception e) {
            log.error("Failed to fetch account details from Oanda API", e);
            throw e;
        }
    }

    public OandaOpenTradeResponse openTrade(String accountId, MarketOrderRequest orderRequest) throws Exception {
        log.info("Opening trade: {}", orderRequest);
        String url = apiUrl + "/v3/accounts/" + accountId + "/orders";

        OandaOrder order = new OandaOrder(orderRequest);

        ObjectMapper objectMapper = new ObjectMapper();
        String reqJson = objectMapper.writeValueAsString(order);

        log.trace("Request JSON: {}", reqJson);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), reqJson);

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String response = res.body().string();
            if (!res.isSuccessful()) {
                log.error("Failed to open trade: {}", res);
                throw new DataProviderException("Error response from Oanda API: " + response);
            }

            log.debug("Opened trade: {}", response);
            return objectMapper.readValue(response, OandaOpenTradeResponse.class);
        } catch (Exception e) {
            log.error("Failed to open trade", e);
            throw e;
        }
    }

    public void closeTrade(String accountId, String id) throws Exception {
        log.debug("Closing trade: {}", id);
        String url = apiUrl + "/v3/accounts/" + accountId + "/trades/" + id + "/close";

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .put(RequestBody.create(MediaType.parse("application/json"), ""))
                .build();

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                log.error("Failed to close trade: {}", res);
                throw new DataProviderException("Error response from Oanda API: " + res.body().string());
            }

            log.debug("Closed trade: {}", id);
        } catch (Exception e) {
            log.error("Failed to close trade", e);
            throw e;
        }

    }

    // Streaming endpoints

    /**
     * Streams prices for the specified instruments and invokes the callback for each price update.
     *
     * @param instruments the list of instruments to stream prices for
     * @param callback    the callback to be invoked for each price update
     */
    public void streamPrices(String accountId, List<Instrument> instruments, PriceStreamCallback callback) {
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
     * Streams transactions for the account and invokes the callback for each transaction update.
     *
     * @param accountId the account id to stream transactions for
     * @param callback  the callback to be invoked for each transaction update
     */
    public void streamTransactions(String accountId, TransactionStreamCallback callback) throws IOException {
        log.trace("Streaming transactions for account: {}", accountId);
        String url = String.format("%s/v3/accounts/%s/transactions/stream", streamUrl, accountId);

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(req).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                String line;
                while ((line = reader.readLine()) != null && !Thread.currentThread().isInterrupted()) {
                    JsonNode jsonNode = objectMapper.readTree(line);
                    if (jsonNode.has("type") && "ORDER_FILL".equals(jsonNode.get("type").asText()) &&
                            jsonNode.has("reason") && "MARKET_ORDER_TRADE_CLOSE".equals(jsonNode.get("reason").asText())) {

                        log.trace("Received ORDER_FILL transaction with MARKET_ORDER_TRADE_CLOSE reason: {}", line);
                        OrderFillTransaction transaction = objectMapper.treeToValue(jsonNode, OrderFillTransaction.class);

                        callback.onOrderFillMarketClose(transaction);
                    }

                    if (line.contains("error")) {
                        throw new IOException("Error in stream: " + line);
                    }
                }
            }
        } catch (Exception e) {
            callback.onError(e);
            throw e;  // Re-throw to signal the stream has ended
        } finally {
            callback.onComplete();
        }

    }


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

    /**
     * Callback interface for handling transaction stream updates.
     */
    public interface TransactionStreamCallback {
        /**
         * Called when a new transaction is received.
         *
         * @param transactionResponse the transaction
         */
        void onOrderFillMarketClose(OrderFillTransaction transactionResponse);

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