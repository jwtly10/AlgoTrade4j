package dev.jwtly10.liveapi.service.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.risk.DailyEquity;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Optional;

@Slf4j
public class RiskManagementServiceClient {
    private final String apiKey;
    private final String apiUrl;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public RiskManagementServiceClient(String apiUrl, String apiKey, OkHttpClient client, ObjectMapper objectMapper) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public RiskManagementServiceClient(String apiUrl, String apiKey, ObjectMapper objectMapper){
        this(apiUrl, apiKey, new OkHttpClient(), objectMapper);
    }

    public Optional<DailyEquity> getDailyEquity(String accountId) throws Exception {
        log.trace("Fetching daily equity from external service for account: {}", accountId);

        String url = apiUrl + "/equity/latest?accountId=" + accountId;

        Request req = new okhttp3.Request.Builder()
                .url(url)
                .header("x-api-key", apiKey)
                .build();

        // TODO: Handle no equity data found
        try (Response res = client.newCall(req).execute()){
            String response = res.body().string();
            if (!res.isSuccessful()){
                throw new RuntimeException("Failed to fetch daily equity from external service. Response: " + response);
            }

            log.debug("Fetched daily equity from external service for account: {} : {}", accountId, response);
            return Optional.of(objectMapper.readValue(response, DailyEquity.class));
        }
    }
}
