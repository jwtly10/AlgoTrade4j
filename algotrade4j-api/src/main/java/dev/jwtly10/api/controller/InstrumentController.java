package dev.jwtly10.api.controller;

import dev.jwtly10.core.model.Instrument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/instruments")
@Slf4j
public class InstrumentController {

    @GetMapping
    public List<String> getAllInstruments() {
        return Arrays.stream(Instrument.values())
                .map(Instrument::name)
                .collect(Collectors.toList());
    }
}