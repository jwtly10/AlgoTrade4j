package dev.jwtly10.liveapi.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimezoneDTO {
    private String name;
    private String zoneId;
}