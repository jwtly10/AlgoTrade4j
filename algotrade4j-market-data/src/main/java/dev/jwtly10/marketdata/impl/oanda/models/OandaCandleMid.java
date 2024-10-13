package dev.jwtly10.marketdata.impl.oanda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OandaCandleMid(String o, String h, String l, String c) {
}