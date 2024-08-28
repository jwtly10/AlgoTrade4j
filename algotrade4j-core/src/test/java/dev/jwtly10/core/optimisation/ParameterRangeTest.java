package dev.jwtly10.core.optimisation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParameterRangeTest {

    @Test
    void testValidParameterRanges() {
        assertDoesNotThrow(() -> {
            ParameterRange param = new ParameterRange("5", "param1", "0", "10", "1", true);
            param.validate();
        });

        assertDoesNotThrow(() -> {
            ParameterRange param = new ParameterRange("5", "param2", "5", "5", "1", true);
            param.validate();
        });

        assertDoesNotThrow(() -> {
            ParameterRange param = new ParameterRange("1", "param3", "1", "1", "1", true);
            param.validate();
        });

        assertDoesNotThrow(() -> {
            ParameterRange param = new ParameterRange("1.5", "param4", "0.5", "2.5", "0.5", true);
            param.validate();
        });

        assertDoesNotThrow(() -> {
            ParameterRange param = new ParameterRange("1.5", "param6", "1", "2", "1", true);
            param.validate();
        });
    }

    @Test
    void testInvalidParameterRanges() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange("7", "param1", "10", "5", "1", true);
            param.validate();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange("5", "param2", "0", "10", "0", true);
            param.validate();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange("5", "param3", "0", "10", "-1", true);
            param.validate();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange("5", "param4", "0", "10", "11", true);
            param.validate();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange("0.5", "param5", "0", "0.9", "1", true);
            param.validate();
        });
    }

    @Test
    void testEdgeCases() {
        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange("0.5", "param1", "0", "1", "0.000000000000001", true);
            param.validate();
        });

        assertDoesNotThrow(() -> {
            ParameterRange param = new ParameterRange("0.5", "param2", "0", "0.9999999999999999", "1", true);
            param.validate();
        });

        String maxDouble = String.valueOf(Double.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange(maxDouble, "param3", "0", maxDouble, "1", true);
            param.validate();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange("-0.0000000000000005", "param4", "-0.000000000000001", "0", "0.000000000000001", true);
            param.validate();
        });
    }

    @Test
    void testSpecialCases() {
        // Not selected (should not throw regardless of values)
        assertDoesNotThrow(() -> {
            ParameterRange param = new ParameterRange("5", "param1", "10", "0", "-1", false);
            param.validate();
        });

        // Selected but with invalid values (should throw)
        assertThrows(IllegalArgumentException.class, () -> {
            ParameterRange param = new ParameterRange("5", "param2", "10", "0", "-1", true);
            param.validate();
        });

        // Default value outside range (should not throw as it's not validated)
        assertDoesNotThrow(() -> {
            ParameterRange param = new ParameterRange("20", "param3", "0", "10", "1", true);
            param.validate();
        });
    }

    @Test
    void testInvalidInputFormats() {
        assertThrows(NumberFormatException.class, () -> {
            ParameterRange param = new ParameterRange("5", "param1", "abc", "10", "1", true);
            param.validate();
        });

        assertThrows(NumberFormatException.class, () -> {
            ParameterRange param = new ParameterRange("5", "param2", "0", "def", "1", true);
            param.validate();
        });

        assertThrows(NumberFormatException.class, () -> {
            ParameterRange param = new ParameterRange("5", "param3", "0", "10", "ghi", true);
            param.validate();
        });

        assertThrows(NumberFormatException.class, () -> {
            ParameterRange param = new ParameterRange("5", "param4", "", "10", "1", true);
            param.validate();
        });
    }
}