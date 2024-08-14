package dev.jwtly10.api.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.model.Number;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class StrategyConfig {
    private String strategyClass;
    private Number initialCash;
    private String symbol;
    private String period;
    private DataSpeed speed;
    private Number spread;
    private Timeframe timeframe;
    private Map<String, String> runParams;

    @Data
    public static class Timeframe {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
        private LocalDateTime from;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
        private LocalDateTime to;
    }
}