package dev.jwtly10.marketdata.impl.oanda.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.marketdata.common.TradeDTO;
import dev.jwtly10.marketdata.common.stream.RetryableStream;
import dev.jwtly10.marketdata.impl.oanda.response.transaction.OrderFillTransaction;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: We may need to support other transaction types in the future, so this will need to be refactored
 * OandaTransactionStream is a stream that listens to the Oanda transaction stream and triggers a callback when a trade is closed
 */
@Slf4j
public class OandaTransactionStream extends RetryableStream<List<TradeDTO>> {
    public OandaTransactionStream(OkHttpClient client, String apiKey, String streamUrl, String accountId, ObjectMapper objectMapper) {
        super(client, buildRequest(apiKey, streamUrl, accountId), objectMapper);
    }

    private static Request buildRequest(String apiKey, String streamUrl, String accountId) {
        String url = String.format("%s/v3/accounts/%s/transactions/stream", streamUrl, accountId);

        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /**
     * Processes the line from the transaction stream into a list of closed trade IDs
     * Callers will then need to verify these trades exist and act accordingly
     *
     * @param line     the line from the transaction stream
     * @param callback the callback to trigger when a trade is closed
     * @throws Exception if there is an error in the transaction stream
     */
    @Override
    protected void processLine(String line, StreamCallback<List<TradeDTO>> callback) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(line);

        if (jsonNode.has("type")) {
            String type = jsonNode.get("type").asText();
            String reason = jsonNode.has("reason") ? jsonNode.get("reason").asText() : "";

            switch (type) {
                case "ORDER_FILL":
                    switch (reason) {
                        case "MARKET_ORDER_TRADE_CLOSE" -> {
                            log.debug("Received ORDER_FILL transaction with MARKET_ORDER_TRADE_CLOSE reason: {}", line);
                            OrderFillTransaction transaction = objectMapper.treeToValue(jsonNode, OrderFillTransaction.class);
                            List<TradeDTO> closedTrades = handleOrderFillTransaction(transaction);

                            callback.onData(closedTrades);
                        }
                        case "STOP_LOSS_ORDER" -> {
                            log.debug("Received ORDER_FILL transaction with STOP_LOSS_ORDER reason: {}", line);
                            OrderFillTransaction transaction = objectMapper.treeToValue(jsonNode, OrderFillTransaction.class);
                            List<TradeDTO> closedTrades = handleOrderFillTransaction(transaction);

                            callback.onData(closedTrades);
                        }
                        case "TAKE_PROFIT_ORDER" -> {
                            log.debug("Received ORDER_FILL transaction with TAKE_PROFIT_ORDER reason: {}", line);
                            OrderFillTransaction transaction = objectMapper.treeToValue(jsonNode, OrderFillTransaction.class);
                            List<TradeDTO> closedTrades = handleOrderFillTransaction(transaction);

                            callback.onData(closedTrades);
                        }
                        case null, default -> log.debug("Received ORDER_FILL transaction with reason: {}", reason);
                    }
                    break;
                case "HEARTBEAT":
                    log.trace("Received HEARTBEAT: {}", line);
                    break;
                default:
                    // We don't really need to handle these, its just for debugging, we will accept those we care about
                    log.trace("Unhandled transaction type: {}", type);
            }
        }

        if (line.contains("error")) {
            throw new Exception("Error in transaction stream: " + line);
        }
    }

    private List<TradeDTO> handleOrderFillTransaction(OrderFillTransaction transaction) {
        List<TradeDTO> closedTrades = new ArrayList<>();

        transaction.tradesClosed().forEach(tradeClose -> {
            log.debug("Trade closed: {}", tradeClose);
            closedTrades.add(new TradeDTO(tradeClose.tradeID(), Double.parseDouble(tradeClose.realizedPL()), Double.parseDouble(tradeClose.price())));
        });

        return closedTrades;
    }
}