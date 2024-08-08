package dev.jwtly10.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The Number class provides a consistent representation of numerical values within the system.
 * It wraps BigDecimal to ensure precision and consistent rounding behavior across all calculations.
 * This class is immutable and thread-safe.
 */
@Getter
@EqualsAndHashCode
@ToString
public class Number implements Comparable<Number> {
    /**
     * The number of decimal places to use for all Number instances.
     */
    public static final int DECIMAL_PLACES = 2;

    /**
     * The rounding mode to use for all Number operations.
     */
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * A constant representing zero.
     */
    public static final Number ZERO = new Number(BigDecimal.ZERO);

    /**
     * The underlying BigDecimal value.
     */
    private final BigDecimal value;

    /**
     * Constructs a Number from a BigDecimal value.
     *
     * @param value The BigDecimal value to wrap
     */
    public Number(BigDecimal value) {
        this.value = value.setScale(DECIMAL_PLACES, ROUNDING_MODE);
    }

    /**
     * Constructs a Number from a String representation of a numeric value.
     *
     * @param value The String representation of the numeric value
     */
    public Number(String value) {
        this(new BigDecimal(value));
    }

    /**
     * Constructs a Number from a double value.
     *
     * @param value The double value to convert to a Number
     */
    public Number(double value) {
        this(BigDecimal.valueOf(value));
    }

    /**
     * Adds another Number to this Number.
     *
     * @param other The Number to add
     * @return A new Number representing the sum
     */
    public Number add(Number other) {
        return new Number(this.value.add(other.value));
    }

    /**
     * Subtracts another Number from this Number.
     *
     * @param other The Number to subtract
     * @return A new Number representing the difference
     */
    public Number subtract(Number other) {
        return new Number(this.value.subtract(other.value));
    }

    /*
     * Returns the absolute value of this Number.
     */
    public Number abs() {
        return new Number(this.value.abs());
    }

    /**
     * Multiplies this Number by a BigDecimal multiplier.
     *
     * @param multiplier The BigDecimal to multiply by
     * @return A new Number representing the product
     */
    public Number multiply(BigDecimal multiplier) {
        return new Number(this.value.multiply(multiplier));
    }

    /**
     * Divides this Number by a BigDecimal divisor.
     *
     * @param divisor The BigDecimal to divide by
     * @return A new Number representing the quotient
     */
    public Number divide(BigDecimal divisor) {
        return new Number(this.value.divide(divisor, ROUNDING_MODE));
    }

    /**
     * Divides this Number by an integer divisor.
     *
     * @param divisor The integer to divide by
     * @return A new Number representing the quotient
     */
    public Number divide(int divisor) {
        return new Number(this.value.divide(new Number(divisor).getValue(), ROUNDING_MODE));
    }

    /**
     * Rounds this Number to the specified number of decimal places.
     *
     * @param scale        The number of decimal places to round to
     * @param roundingMode The rounding mode to use
     * @return A new Number representing the rounded value
     */
    public Number setScale(int scale, RoundingMode roundingMode) {
        return new Number(this.value.setScale(scale, roundingMode));
    }

    /**
     * Converts this Number to a double value.
     *
     * @return The double value of this Number
     */
    public double doubleValue() {
        return this.value.doubleValue();
    }

    /**
     * Checks if this Number is greater than another Number.
     *
     * @param other The Number to compare against
     * @return true if this Number is greater, false otherwise
     */
    public boolean isGreaterThan(Number other) {
        return this.value.compareTo(other.value) > 0;
    }

    /**
     * Checks if this Number is less than another Number.
     *
     * @param other The Number to compare against
     * @return true if this Number is less, false otherwise
     */
    public boolean isLessThan(Number other) {
        return this.value.compareTo(other.value) < 0;
    }

    /**
     * Compares this Number with another Number for ordering.
     *
     * @param other The Number to be compared
     * @return a negative integer, zero, or a positive integer as this Number
     * is less than, equal to, or greater than the specified Number.
     */
    @Override
    public int compareTo(Number other) {
        return this.value.compareTo(other.value);
    }

    public boolean isEquals(Number other) {
        return this.value.compareTo(other.value) == 0;
    }
}