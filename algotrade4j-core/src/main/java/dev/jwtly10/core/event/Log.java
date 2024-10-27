package dev.jwtly10.core.event;

import dev.jwtly10.core.event.types.LogEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@ToString
public class Log {
    private String message;
    private LogEvent.LogType type;
    private ZonedDateTime time;
}