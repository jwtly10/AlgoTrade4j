package dev.jwtly10.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class IndicatorValue {
    private Number value;
    private ZonedDateTime dateTime;
}