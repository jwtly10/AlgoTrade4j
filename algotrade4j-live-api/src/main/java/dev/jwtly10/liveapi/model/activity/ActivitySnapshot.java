package dev.jwtly10.liveapi.model.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySnapshot {
    private String description;
    private ZonedDateTime timestamp;
}