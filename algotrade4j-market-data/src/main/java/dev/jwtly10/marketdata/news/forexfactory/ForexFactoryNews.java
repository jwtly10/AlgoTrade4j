package dev.jwtly10.marketdata.news.forexfactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import dev.jwtly10.marketdata.news.GenericNewsEvent;
import dev.jwtly10.marketdata.news.NewsImpact;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
        public static final Impact HOLIDAY = new Impact("Holiday");
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
                case "holiday":
                    return HOLIDAY;
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

    public NewsImpact convertImpact(){
        switch (impact.value) {
            case "Medium":
                return NewsImpact.MEDIUM;
            case "High":
                return NewsImpact.HIGH;
            default:
                // TODO: Does this work? Are there any dangerous events that are not marked as HIGH?
                return NewsImpact.LOW;
        }
    }

    /**
     * Converts the ForexFactoryNews to a GenericNewsEvent.
     *
     * @return The GenericNewsEvent.
     */
    public GenericNewsEvent toGenericNewsEvent(){
        return new GenericNewsEvent(
                country,
                convertImpact(),
                title,
                ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC)
        );

    }
}