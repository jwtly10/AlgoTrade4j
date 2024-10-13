package dev.jwtly10.backtestapi.controller.data;

import dev.jwtly10.backtestapi.model.marketData.MarketDataBarDTO;
import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.Period;
import dev.jwtly10.marketdata.common.Broker;
import dev.jwtly10.marketdata.common.ClientCallback;
import dev.jwtly10.marketdata.impl.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.impl.oanda.OandaClient;
import dev.jwtly10.marketdata.impl.oanda.OandaDataClient;
import dev.jwtly10.shared.config.ratelimit.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
 * TODO: Should also change auth to use spring security w/ filters & use @Cacheable, but this works as POC
 */
@RestController
@RequestMapping("/api/v1/marketdata")
@Slf4j
public class MarketDataController {

    private final OandaDataClient oandaDataClient;

    @Value("${marketdata.api.key}")
    private String apiKey;

    private final CacheManager cacheManager;

    public MarketDataController(OandaClient oandaClient, CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        OandaBrokerClient oandaBrokerClient = new OandaBrokerClient(oandaClient, null);
        this.oandaDataClient = new OandaDataClient(oandaBrokerClient);
    }

    @GetMapping("/candles")
    @RateLimit(limit = 10)
    public ResponseEntity<?> getCandles(
            @RequestHeader(value = "x-api-key", required = false) String requestApiKey,
            @RequestParam("broker") Broker broker,
            @RequestParam("instrument") Instrument instrument,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("period") Period period) {

        if (!this.apiKey.equals(requestApiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Missing or invalid API key");
        }

        String cacheKey = broker + "-" + instrument + "-" + from + "-" + to + "-" + period;

        Cache candlesCache = cacheManager.getCache("candlesCache");
        List<MarketDataBarDTO> cachedResult = candlesCache.get(cacheKey, List.class);

        if (cachedResult != null) {
            log.info("Returning cached result for key: {}", cacheKey);
            return ResponseEntity.ok(cachedResult);
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

        candlesCache.put(cacheKey, result);

        return ResponseEntity.ok(result);
    }
}