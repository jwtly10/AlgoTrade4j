package dev.jwtly10.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This test is just a general test to ensure that any strategies we have used in the system can actually be found at runtime
 * Although its limited to 'Public' strategies. Private strategies will not be found
 */
class StrategyReflectionUtilsTest {

    @Test
    void testThrowsInvalidStrategy() {
        assertThrows(Exception.class, () -> StrategyReflectionUtils.getStrategyFromClassName("Invalid Class name", ""));
    }

    @Test
    void testSMACrossoverStrategy() throws Exception {
        StrategyReflectionUtils.getStrategyFromClassName("SMACrossoverStrategy", "");
    }
}