package dev.jwtly10.marketdata.news.forexfactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Represents news from ForexFactory.
 *
 * @param title    The title of the news.
 * @param country  The country associated with the news.
 * @param date     The date and time of the news, converted to UTC.
 * @param impact   The impact level of the news.
 * @param forecast The forecast value associated with the news. May be null, a double or a %
 * @param previous The previous value associated with the news. May be null, a double or a %
 */
public record ForexFactoryNews(
        String title,
        String country,
        OffsetDateTime date,
        Impact impact,
        String forecast,
        String previous
) {

    /**
     * Constructor for ForexFactoryNews.
     * Converts the date to UTC.
     */
    public ForexFactoryNews {
        // Convert date to UTC
        date = date.withOffsetSameInstant(ZoneOffset.UTC);
    }

    /**
     * Represents the impact level of the news.
     */
    public static class Impact {
        public static final Impact LOW = new Impact("Low");
        public static final Impact MEDIUM = new Impact("Medium");
        public static final Impact HIGH = new Impact("High");
        private final String value;

        /**
         * Private constructor for Impact.
         *
         * @param value The impact level as a string.
         */
        private Impact(String value) {
            this.value = value;
        }

        /**
         * Creates an Impact instance from a string value.
         *
         * @param value The impact level as a string.
         * @return The corresponding Impact instance.
         */
        @JsonCreator
        public static Impact fromString(String value) {
            if (value == null) {
                return null;
            }
            switch (value.toLowerCase()) {
                case "low":
                    return LOW;
                case "medium":
                    return MEDIUM;
                case "high":
                    return HIGH;
                default:
                    return new Impact(value);
            }
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}