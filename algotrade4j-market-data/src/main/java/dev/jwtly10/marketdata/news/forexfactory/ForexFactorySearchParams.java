package dev.jwtly10.marketdata.news.forexfactory;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ForexFactorySearchParams(
        String country,
        ForexFactoryNews.Impact impact,
        LocalDate date
) {
}