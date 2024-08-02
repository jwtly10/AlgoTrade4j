package dev.jwtly10.core.datafeed;

import dev.jwtly10.core.Bar;

import java.time.Duration;

public interface CsvParseFormat {
    boolean hasHeader();

    String getDelimiter();

    Bar parseBar(String symbol, String[] fields);

    Duration timePeriod();
}