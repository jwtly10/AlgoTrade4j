package dev.jwtly10.backtestapi.model.marketData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BarDTO {
    private String instrument;
    private ZonedDateTime openTime;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
}