package dev.jwtly10.marketdata.common;

import dev.jwtly10.core.model.Bar;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Iterator;

public interface ExternalDataClient {
    Iterator<Bar> fetchCandlesIterator(String instrument, ZonedDateTime from, ZonedDateTime to, Duration period);
}