package dev.jwtly10.marketdata.oanda.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PriceBucket(String price, int liquidity) {
}