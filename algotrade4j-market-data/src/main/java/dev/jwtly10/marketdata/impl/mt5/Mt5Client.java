package dev.jwtly10.marketdata.impl.mt5;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.model.TradeParameters;
import dev.jwtly10.marketdata.impl.mt5.models.Mt5Login;
import dev.jwtly10.marketdata.impl.mt5.models.Mt5Trade;
import dev.jwtly10.marketdata.impl.mt5.response.Mt5AccountResponse;
import dev.jwtly10.marketdata.impl.mt5.response.Mt5TradesResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class Mt5Client {
    private final String apiKey;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    public Mt5Client(String apiKey, String apiUrl, OkHttpClient client, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
        this.client = client;
    }

    public Mt5Client(String apiKey, String apiUrl, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
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
    public void initialiseAccount(int accountId, String password, String server, String path) throws Exception {
        log.info("Initialising MT5 account: {}", accountId);
        String url = String.format("%s/initialise", apiUrl);

        Mt5Login login = new Mt5Login(accountId, password, server, path);

        String body = objectMapper.writeValueAsString(login);
        log.trace("Request JSON: {}", body);

        RequestBody reqBody = RequestBody.create(body, okhttp3.MediaType.parse("application/json"));

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
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
    public Mt5AccountResponse fetchAccount(String accountId) throws Exception {
        log.trace("Fetching account info for account: {}", accountId);

        String url = String.format("%s/accounts/%s", apiUrl, accountId);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Failed to fetch account info. Response code: {}, Response Body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to fetch account info: " + responseBody);
            }

            return objectMapper.readValue(responseBody, Mt5AccountResponse.class);
        }
    }

    /**
     * Fetch the trades for the given account
     *
     * @param accountId the account id of the mt5 terminal
     * @return the trades for the given account
     * @throws Exception if the trades cannot be fetched
     */
    public Mt5TradesResponse fetchTrades(String accountId) throws Exception {
        log.trace("Fetching trades for account: {}", accountId);

        String url = String.format("%s/trades/%s", apiUrl, accountId);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Failed to fetch trades. Response code: {}, Response Body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to fetch trades: " + responseBody);
            }

            return objectMapper.readValue(responseBody, Mt5TradesResponse.class);
        }
    }

    /**
     * Open a trade for the given account
     *
     * @param accountId   the account id of the mt5 terminal
     * @param tradeParams the trade parameters for the trade
     * @return the opened trade
     */
    public Mt5Trade openTrade(String accountId, TradeParameters tradeParams) throws Exception {
        log.trace("Opening trade for account: {}", accountId);

        String url = String.format("%s/trades/open/%s", apiUrl, accountId);

        String body = objectMapper.writeValueAsString(tradeParams);
        log.trace("Request JSON: {}", body);
        RequestBody reqBody = RequestBody.create(body, okhttp3.MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(reqBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Failed to open trade. Response code: {}, Response Body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to open trade: " + responseBody);
            }

            return objectMapper.readValue(responseBody, Mt5Trade.class);
        }
    }

    /**
     * Close a given trade for an account
     *
     * @param accountId the account id of the mt5 terminal
     * @param tradeId   the trade id to close
     * @return the closed trade
     */
    public Mt5Trade closeTrade(String accountId, Integer tradeId) throws Exception {
        log.trace("Closing trade {} for account: {}", tradeId, accountId);

        String url = String.format("%s/trades/close/%s/%s", apiUrl, accountId, tradeId);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create("", okhttp3.MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                log.error("Failed to close trade. Response code: {}, Response Body: {}", response.code(), responseBody);
                throw new RuntimeException("Failed to close trade: " + responseBody);
            }

            return objectMapper.readValue(responseBody, Mt5Trade.class);
        }
    }
}