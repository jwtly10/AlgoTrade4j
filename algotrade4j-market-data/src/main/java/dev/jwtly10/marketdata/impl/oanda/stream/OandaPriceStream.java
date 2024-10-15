package dev.jwtly10.marketdata.impl.oanda.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.marketdata.common.stream.RetryableStream;
import dev.jwtly10.marketdata.impl.oanda.response.OandaPriceResponse;
import dev.jwtly10.marketdata.impl.oanda.utils.OandaUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: We may need to support other transaction types in the future, so this will need to be refactored
 * OandaPriceStream is a stream that listens to the Oanda prices stream and triggers a callback when a price/tick is received
 */
public class OandaPriceStream extends RetryableStream<Tick> {
    public OandaPriceStream(OkHttpClient client, String apiKey, String streamUrl, String accountId, List<Instrument> instruments, ObjectMapper objectMapper) {
        super(client, buildRequest(apiKey, streamUrl, accountId, instruments), objectMapper);
    }

    private static Request buildRequest(String apiKey, String streamUrl, String accountId, List<Instrument> instruments) {
        String instrumentParams = instruments.stream()
                .map(Instrument::getOandaSymbol)
                .collect(Collectors.joining(","));

        String url = String.format("%s/v3/accounts/%s/pricing/stream?instruments=%s", streamUrl, accountId, instrumentParams);

        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /**
     * Processes the line from the price stream into a Tick object
     *
     * @param line     the line from the price stream
     * @param callback the callback to trigger when a price is received
     * @throws Exception if there is an error in the price stream
     */
    @Override
    protected void processLine(String line, StreamCallback<Tick> callback) throws Exception {
        if (line.contains("\"type\":\"PRICE\"")) {
            OandaPriceResponse priceResponse = objectMapper.readValue(line, OandaPriceResponse.class);
            Tick tick = OandaUtils.mapPriceToTick(priceResponse);
            callback.onData(tick);
        }
        if (line.contains("error")) {
            throw new IOException("Error in price stream: " + line);
        }
    }
}