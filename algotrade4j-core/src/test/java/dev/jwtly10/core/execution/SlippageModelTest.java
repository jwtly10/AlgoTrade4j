package dev.jwtly10.core.execution;

import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlippageModelTest {

    private SlippageModel slippageModel;

    @BeforeEach
    void setUp() {
        slippageModel = new SlippageModel();
    }

    @Test
    void testLongStopLossNormalVolatility() {
        Number result = slippageModel.calculateExecutionPrice(
                true, new Number("100"), new Number("120"),
                new Number("99"), new Number("98"), false);
        assertTrue(result.compareTo(new Number("100")) <= 0 && result.compareTo(new Number("98")) >= 0);
    }

    @Test
    void testLongStopLossHighVolatility() {
        Number result = slippageModel.calculateExecutionPrice(
                true, new Number("100"), new Number("120"),
                new Number("99"), new Number("98"), true);
        assertEquals(new Number("98"), result);
    }

    @Test
    void testLongTakeProfitNormalVolatility() {
        Number result = slippageModel.calculateExecutionPrice(
                true, new Number("100"), new Number("18723.8"),
                new Number("18732"), new Number("18732"), false);
        System.out.println(result);
        assertTrue(result.isLessThan(new Number("18732")) && result.isGreaterThan(new Number("18723.8")));
    }

    @Test
    void testLongTakeProfitHighVolatility() {
        Number result = slippageModel.calculateExecutionPrice(
                true, new Number("100"), new Number("120"),
                new Number("122"), new Number("121"), true);
        assertEquals(new Number("121"), result);
    }

    @Test
    void testShortStopLossNormalVolatility() {
        Number result = slippageModel.calculateExecutionPrice(
                false, new Number("120"), new Number("100"),
                new Number("121"), new Number("120"), false);
        assertTrue(result.compareTo(new Number("120")) >= 0 && result.compareTo(new Number("121")) <= 0);
    }

    @Test
    void testShortStopLossHighVolatility() {
        Number result = slippageModel.calculateExecutionPrice(
                false, new Number("120"), new Number("100"),
                new Number("121"), new Number("120"), true);
        assertEquals(new Number("121"), result);
    }

    @Test
    void testShortTakeProfitNormalVolatility() {
        Number result = slippageModel.calculateExecutionPrice(
                false, new Number("120"), new Number("100"),
                new Number("99"), new Number("98"), false);
        assertTrue(result.compareTo(new Number("99")) >= 0 && result.compareTo(new Number("100")) <= 0,
                "Result " + result + " should be between 99 and 100");
    }

    @Test
    void testShortTakeProfitHighVolatility() {
        Number result = slippageModel.calculateExecutionPrice(
                false, new Number("120"), new Number("100"),
                new Number("99"), new Number("98"), true);
        assertEquals(new Number("99"), result);
    }

    @Test
    void testNoTriggerLong() {
        Number result = slippageModel.calculateExecutionPrice(
                true, new Number("100"), new Number("120"),
                new Number("110"), new Number("109"), false);
        assertNull(result);
    }

    @Test
    void testNoTriggerShort() {
        Number result = slippageModel.calculateExecutionPrice(
                false, new Number("120"), new Number("100"),
                new Number("110"), new Number("109"), false);
        assertNull(result);
    }
}