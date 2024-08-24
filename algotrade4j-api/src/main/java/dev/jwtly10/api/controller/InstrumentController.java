package dev.jwtly10.api.controller;

import dev.jwtly10.core.model.Instrument;
import dev.jwtly10.core.model.InstrumentData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instruments")
@Slf4j
public class InstrumentController {

    @GetMapping
    public ResponseEntity<InstrumentData[]> getAllInstruments() {
        return ResponseEntity.ok(Instrument.getAllInstrumentData());
    }
}