package dev.jwtly10.core.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@Slf4j
public abstract class BaseEvent {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String eventId;
    private final String type;
    private final String symbol;
    private final String strategyId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime timestamp;


    protected BaseEvent(String strategyId, String type, String symbol) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = ZonedDateTime.now();
        this.strategyId = strategyId;
        this.type = type;
        this.symbol = symbol;
    }

    /**
     * Convert event to JSON message
     *
     * @return JSON message
     */
    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}