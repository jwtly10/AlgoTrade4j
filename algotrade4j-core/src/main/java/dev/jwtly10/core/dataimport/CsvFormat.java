package dev.jwtly10.core.dataimport;

import dev.jwtly10.core.Bar;

import java.time.Duration;

public interface CsvFormat {
    boolean hasHeader();
    String getDelimiter();
    Bar parseBar(String[] fields);
    Duration getTimePeriod();
}