package dev.jwtly10.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentData {
    private String internalSymbol;
    private String oandaSymbol;
    private int decimalPlaces;
    private double minimumMove;

    public Instrument getInstrument() {
        return Instrument.valueOf(internalSymbol);
    }
}