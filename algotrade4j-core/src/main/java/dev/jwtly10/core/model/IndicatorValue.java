package dev.jwtly10.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndicatorValue {
    private Number value;
    private ZonedDateTime dateTime;
}