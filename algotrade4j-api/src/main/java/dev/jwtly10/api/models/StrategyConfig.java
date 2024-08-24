package dev.jwtly10.api.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.jwtly10.core.data.DataSpeed;
import dev.jwtly10.core.model.InstrumentData;
import dev.jwtly10.core.model.Number;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategyConfig {
    private String strategyClass;
    private Number initialCash;
    private InstrumentData instrumentData;
    private String period;
    private DataSpeed speed;
    private Number spread;
    private Timeframe timeframe;
    private List<RunParameter> runParams;

    @Data
    public static class Timeframe {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
        private LocalDateTime from;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssX")
        private LocalDateTime to;
    }

    @Data
    public static class RunParameter {
        private String name;
        private String value;
        private String description;
        private String defaultValue;
        private String start;
        private String stop;
        private String step;
        private Boolean selected;
    }
}