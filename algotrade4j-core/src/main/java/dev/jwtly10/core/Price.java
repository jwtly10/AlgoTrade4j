package dev.jwtly10.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@EqualsAndHashCode
@ToString
public class Price implements Comparable<Price> {
    private static final int DECIMAL_PLACES = 2;
    private final BigDecimal value;

    public Price(BigDecimal value) {
        this.value = value.setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }

    public Price(String value) {
        this(new BigDecimal(value));
    }

    public Price(double value) {
        this(BigDecimal.valueOf(value));
    }

    public Price add(Price other) {
        return new Price(this.value.add(other.value));
    }

    public Price subtract(Price other) {
        return new Price(this.value.subtract(other.value));
    }

    public Price multiply(BigDecimal multiplier) {
        return new Price(this.value.multiply(multiplier));
    }

    public Price divide(BigDecimal divisor) {
        return new Price(this.value.divide(divisor, RoundingMode.HALF_UP));
    }

    public boolean isGreaterThan(Price other) {
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(Price other) {
        return this.value.compareTo(other.value) < 0;
    }

    @Override
    public int compareTo(Price other) {
        return this.value.compareTo(other.value);
    }

    public static Price ZERO = new Price(BigDecimal.ZERO);
}