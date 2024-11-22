package dev.jwtly10.marketdata.news;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class GenericNewsEvent {
    private String country;
    private NewsImpact impact;
    private String title;
    private ZonedDateTime dateTime;
}