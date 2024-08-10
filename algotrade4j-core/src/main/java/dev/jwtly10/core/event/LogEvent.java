package dev.jwtly10.core.event;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class LogEvent extends BaseEvent {
    private final String message;
    private final LogType logType;
    private final ZonedDateTime time;

    public LogEvent(String strategyId, LogType type, String messageTemplate, Object... params) {
        super(strategyId, "LOG", null);
        this.logType = type;
        this.message = formatMessage(messageTemplate, params);
        this.time = ZonedDateTime.now();
    }

    public LogEvent(String strategyId, LogType logType, String messageTemplate, Throwable exception, Object... params) {
        super(strategyId, "LOG", null);
        this.logType = logType;
        String formattedMessage = formatMessage(messageTemplate, params);
        this.message = exception != null
                ? formattedMessage + " - Error: " + exception.getMessage()
                : formattedMessage;
        this.time = ZonedDateTime.now();
    }

    private String formatMessage(String messageTemplate, Object[] params) {
        if (params == null || params.length == 0) {
            return messageTemplate;
        }
        return String.format(messageTemplate.replace("{}", "%s"), params);
    }

    public enum LogType {
        INFO,
        WARNING,
        ERROR
    }
}