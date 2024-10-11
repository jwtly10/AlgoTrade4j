package dev.jwtly10.backtestapi.controller.data;

import dev.jwtly10.backtestapi.model.marketData.MarketDataBarDTO;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Period;
import dev.jwtly10.marketdata.common.Broker;
import dev.jwtly10.marketdata.common.ClientCallback;
import dev.jwtly10.marketdata.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.oanda.OandaClient;
import dev.jwtly10.marketdata.oanda.OandaDataClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Exposes some of the internal data logic to external systems.
 * Used for python SDK which allows for very quick prototyping of strategies.
 * This is a read-only endpoint.
 * TODO: We should use DI and factory patterns to support passing in a broker and using that specifically
 */
@RestController
@RequestMapping("/api/v1/marketdata")
@Slf4j
public class MarketDataController {
    private final OandaDataClient oandaDataClient;

    @Value("${marketdata.api.key}")
    private String apiKey;

    public MarketDataController(OandaClient oandaClient) {
        // Internal logic let us use the fetch data endpoint even if account id is not set
        OandaBrokerClient oandaBrokerClient = new OandaBrokerClient(oandaClient, null);
        this.oandaDataClient = new OandaDataClient(oandaBrokerClient);
    }

    @GetMapping("/candles")
    @Cacheable(value = "candlesCache", key = "#instrument + '-' + #from + '-' + #to + '-' + #period", unless = "#result.body == null || #result.body.isEmpty()")
    public ResponseEntity<?> getCandles(
            @RequestHeader("x-api-key") String requestApiKey,
            @RequestParam("broker") Broker broker,
            @RequestParam("instrument") Instrument instrument,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("period") Period period) {

        if (!this.apiKey.equals(requestApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API key");
        }

        ZonedDateTime fromDateTime = ZonedDateTime.parse(from);
        ZonedDateTime toDateTime = ZonedDateTime.parse(to);
        Duration duration = period.getDuration();

        List<MarketDataBarDTO> result = new ArrayList<>();

        try {
            switch (broker) {
                case OANDA:
                    oandaDataClient.fetchCandles(instrument, fromDateTime, toDateTime, duration, new ClientCallback() {
                        @Override
                        public boolean onCandle(Bar bar) {
                            MarketDataBarDTO dto = new MarketDataBarDTO(
                                    bar.getInstrument().getOandaSymbol(),
                                    bar.getOpenTime(),
                                    bar.getOpen().doubleValue(),
                                    bar.getHigh().doubleValue(),
                                    bar.getLow().doubleValue(),
                                    bar.getClose().doubleValue(),
                                    bar.getVolume().doubleValue()
                            );
                            result.add(dto);
                            return true;
                        }

                        @Override
                        public void onError(Exception exception) {
                            log.error("Error fetching data", exception);
                            throw new RuntimeException("Error fetching data", exception);
                        }

                        @Override
                        public void onComplete() {
                            log.debug("Fetching complete.");
                        }
                    });
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid broker");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}