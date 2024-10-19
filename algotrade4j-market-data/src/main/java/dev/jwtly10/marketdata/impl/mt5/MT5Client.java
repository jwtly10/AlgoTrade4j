package dev.jwtly10.marketdata.impl.mt5;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.marketdata.impl.mt5.models.MT5Login;
import dev.jwtly10.marketdata.impl.mt5.models.MT5Trade;
import dev.jwtly10.marketdata.impl.mt5.request.MT5TradeRequest;
import dev.jwtly10.marketdata.impl.mt5.response.MT5AccountResponse;
import dev.jwtly10.marketdata.impl.mt5.response.MT5TradesResponse;
import dev.jwtly10.marketdata.impl.mt5.stream.MT5TransactionStream;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class MT5Client {
    private final String apiKey;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    private final static String API_KEY_HEADER = "x-api-key";

    public MT5Client(String apiKey, String apiUrl, OkHttpClient client, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl + "/api/v1";
        this.objectMapper = objectMapper;
        this.client = client;
    }

    public MT5Client(String apiKey, String apiUrl, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl + "/api/v1";
        this.objectMapper = objectMapper;
        this.client = new OkHttpClient();
    }

    /**
     * Initialise the account with the given credentials
     * This is required before any other operations can be performed
     *
     * @param accountId the account id of the mt5 terminal
     * @param password  the password of the mt5 terminal
     * @param server    the server name of the mt5 terminal
     * @param path      the path to the mt5 terminal64.exe
     * @throws Exception if the account cannot be initialised
     */
    public void initializeAccount(int accountId, String password, String server, String path) throws Exception {
        log.info("Initializing MT5 account: {}", accountId);
        String url = String.format("%s/initialize", apiUrl);

        MT5Login login = new MT5Login(accountId, password, server, path);

        String body = objectMapper.writeValueAsString(login);
        log.trace("Request JSON: {}", body);

        RequestBody reqBody = RequestBody.create(body, okhttp3.MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, apiKey)
                .post(reqBody)
                .build();

        try (Response response = client.newCall(req).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                log.error("Failed to initialise account. Response code: {}, Response body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to initialise account: " + responseBody);
            }

            log.info("Account initialised: {}", responseBody);
        }
    }

    /**
     * Fetch the account info for the given account
     *
     * @param accountId the account id of the mt5 terminal
     * @return the account info for the given account
     * @throws Exception if the account info cannot be fetched
     */
    public MT5AccountResponse fetchAccount(String accountId) throws Exception {
        log.trace("Fetching account info for account: {}", accountId);

        String url = String.format("%s/accounts/%s", apiUrl, accountId);

        Request request = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Failed to fetch account info. Response code: {}, Response Body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to fetch account info: " + responseBody);
            }

            return objectMapper.readValue(responseBody, MT5AccountResponse.class);
        }
    }

    /**
     * Fetch the trades for the given account
     *
     * @param accountId the account id of the mt5 terminal
     * @return the trades for the given account
     * @throws Exception if the trades cannot be fetched
     */
    public MT5TradesResponse fetchTrades(String accountId) throws Exception {
        log.trace("Fetching trades for account: {}", accountId);

        String url = String.format("%s/trades/%s", apiUrl, accountId);

        Request request = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Failed to fetch trades. Response code: {}, Response Body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to fetch trades: " + responseBody);
            }

            return objectMapper.readValue(responseBody, MT5TradesResponse.class);
        }
    }

    /**
     * Open a trade for the given account
     *
     * @param accountId the account id of the mt5 terminal
     * @param tradeReq  the trade parameters for the trade
     * @return the opened trade
     */
    public MT5Trade openTrade(String accountId, MT5TradeRequest tradeReq) throws Exception {
        log.trace("Opening trade for account: {}", accountId);

        String url = String.format("%s/trades/%s/open", apiUrl, accountId);

        String body = objectMapper.writeValueAsString(tradeReq);
        log.info("Request JSON: {}", body);
        RequestBody reqBody = RequestBody.create(body, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, apiKey)
                .post(reqBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Failed to open trade. Response code: {}, Response Body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to open trade: " + responseBody);
            }

            return objectMapper.readValue(responseBody, MT5Trade.class);
        }
    }

    /**
     * Close a given trade for an account
     *
     * @param accountId the account id of the mt5 terminal
     * @param tradeId   the trade id to close
     */
    public void closeTrade(String accountId, Integer tradeId) throws Exception {
        log.trace("Closing trade {} for account: {}", tradeId, accountId);

        String url = String.format("%s/trades/%s/close/%s", apiUrl, accountId, tradeId);

        Request request = new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, apiKey)
                .post(RequestBody.create("", okhttp3.MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Failed to close trade. Response code: {}, Response Body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to close trade: " + responseBody);
            }
        }
    }

    public MT5TransactionStream streamTransactions(String accountId) {
        return new MT5TransactionStream(client, apiKey, apiUrl, accountId, objectMapper);
    }
}