package dev.jwtly10.core.external.news.forexfactory;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ForexFactorySearchParams(
        String country,
        ForexFactoryNews.Impact impact,
        LocalDate date
) {
}