package dev.jwtly10.core.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jwtly10.core.model.Instrument;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Abstract base class for events in the system.
 * These events are used to communicate information between components, and potentially external systems.
 */
@Data
@RequiredArgsConstructor
@Slf4j
public abstract class BaseEvent {
    /**
     * ObjectMapper instance for JSON processing.
     */
    protected static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Unique identifier for the event.
     */
    private final String eventId;

    /**
     * Type of the event.
     */
    private final String type;

    /**
     * Instrument associated with the event.
     */
    private final Instrument instrument;

    /**
     * Strategy identifier associated with the event.
     */
    private final String strategyId;

    /**
     * Timestamp of the event.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private ZonedDateTime timestamp;

    /**
     * Constructs a BaseEvent with the specified strategy ID, type, and instrument.
     * Generates a unique event ID and sets the current timestamp.
     *
     * @param strategyId the strategy identifier
     * @param type       the type of the event
     * @param instrument the instrument associated with the event
     */
    protected BaseEvent(String strategyId, String type, Instrument instrument) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = ZonedDateTime.now();
        this.strategyId = strategyId;
        this.type = type;
        this.instrument = instrument;
    }

    /**
     * Converts the event to a JSON message.
     *
     * @return JSON representation of the event
     * @throws JsonProcessingException if an error occurs during JSON processing
     */
    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}