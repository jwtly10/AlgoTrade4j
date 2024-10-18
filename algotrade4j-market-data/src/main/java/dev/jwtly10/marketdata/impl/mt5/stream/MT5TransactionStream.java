package dev.jwtly10.marketdata.impl.mt5.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.marketdata.common.TradeDTO;
import dev.jwtly10.marketdata.common.stream.RetryableStream;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * MT5TransactionStream is a stream that listens to the MT5 transaction stream and triggers a callback when a trade is closed.
 */
@Slf4j
public class MT5TransactionStream extends RetryableStream<List<TradeDTO>> {

    private final static String API_KEY_HEADER = "x-api-key";

    public MT5TransactionStream(OkHttpClient client, String apiKey, String streamUrl, String accountId, ObjectMapper objectMapper) {
        super(client, buildRequest(apiKey, streamUrl, accountId), objectMapper);
    }

    // Build the request to connect to the FastAPI stream
    private static Request buildRequest(String apiKey, String streamUrl, String accountId) {
        String url = String.format("%s/transactions/%s/stream", streamUrl, accountId);
        return new Request.Builder()
                .url(url)
                .addHeader(API_KEY_HEADER, apiKey)
                .build();
    }

    /**
     * Processes each line of the event stream and triggers the callback when a trade is closed.
     *
     * @param line     the line received from the event stream
     * @param callback the callback that will handle the closed trades
     * @throws Exception if there's an error in the event stream
     */
    @Override
    protected void processLine(String line, StreamCallback<List<TradeDTO>> callback) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(line);

        if (jsonNode.has("type") && "CLOSE".equals(jsonNode.get("type").asText())) {
            log.debug("Received CLOSE event: {}", line);
            TradeDTO closedTrade = new TradeDTO(
                    jsonNode.get("position_id").asText(),
                    jsonNode.get("profit").asDouble(),
                    jsonNode.get("close_order_price").asDouble()
            );

            List<TradeDTO> closedTrades = new ArrayList<>();
            closedTrades.add(closedTrade);

            callback.onData(closedTrades);
        } else {
            log.trace("Received unhandled line: {}", line);
        }
        if (line.contains("error")) {
            throw new Exception("Error in transaction stream: " + line);
        }
    }
}