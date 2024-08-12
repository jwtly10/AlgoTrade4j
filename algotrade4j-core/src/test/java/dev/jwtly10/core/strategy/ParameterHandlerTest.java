package dev.jwtly10.core.strategy;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParameterHandlerTest {

    private TestStrategy testStrategy;

    @BeforeEach
    void setUp() {
        testStrategy = new TestStrategy("test");
    }

    @Test
    void testGetParameters() {
        Map<String, String> params = ParameterHandler.getParameters(testStrategy);

        assertEquals(5, params.size());
        assertEquals("10", params.get("intParam"));
        assertEquals("3.14", params.get("doubleParam"));
        assertEquals("default", params.get("stringParam"));
        assertEquals("VALUE2", params.get("enumParam"));
        assertEquals("true", params.get("booleanParam"));
    }

    @Test
    void testValidateParameters() {
        assertDoesNotThrow(() -> ParameterHandler.validateParameters(testStrategy));
    }

    @Test
    void testValidateParametersWithEmptyValue() {
        class InvalidStrategy extends BaseStrategy {
            @Parameter(name = "invalidParam", description = "Invalid parameter", value = "")
            private String invalidParam;

            public InvalidStrategy(String strategyId) {
                super(strategyId);
            }

            @Override
            public void onBarClose(Bar bar) {
            }

            @Override
            public void onTick(Tick tick, Bar currentBar) {
            }
        }

        InvalidStrategy invalidStrategy = new InvalidStrategy("invalid");
        assertThrows(IllegalStateException.class, () -> ParameterHandler.validateParameters(invalidStrategy));
    }

    @Test
    void testSetParameter() throws NoSuchFieldException, IllegalAccessException {
        ParameterHandler.setParameter(testStrategy, "intParam", "20");
        ParameterHandler.setParameter(testStrategy, "doubleParam", "6.28");
        ParameterHandler.setParameter(testStrategy, "stringParam", "newValue");
        ParameterHandler.setParameter(testStrategy, "enumParam", "VALUE3");
        ParameterHandler.setParameter(testStrategy, "booleanParam", "false");

        assertEquals(20, testStrategy.intParam);
        assertEquals(6.28, testStrategy.doubleParam, 0.001);
        assertEquals("newValue", testStrategy.stringParam);
        assertEquals(TestEnum.VALUE3, testStrategy.enumParam);
        assertFalse(testStrategy.booleanParam);
    }

    @Test
    void testSetParameterWithInvalidName() {
        assertThrows(NoSuchFieldException.class, () -> ParameterHandler.setParameter(testStrategy, "nonExistentParam", "value"));
    }

    @Test
    void testInitialize() throws IllegalAccessException {
        ParameterHandler.initialize(testStrategy);

        assertEquals(10, testStrategy.intParam);
        assertEquals(3.14, testStrategy.doubleParam, 0.001);
        assertEquals("default", testStrategy.stringParam);
        assertEquals(TestEnum.VALUE2, testStrategy.enumParam);
        assertTrue(testStrategy.booleanParam);
    }

    @Test
    void testConvertValue() throws NoSuchFieldException, IllegalAccessException {
        ParameterHandler.setParameter(testStrategy, "intParam", "42");
        ParameterHandler.setParameter(testStrategy, "doubleParam", "3.1415");
        ParameterHandler.setParameter(testStrategy, "booleanParam", "false");
        ParameterHandler.setParameter(testStrategy, "enumParam", "VALUE1");

        assertEquals(42, testStrategy.intParam);
        assertEquals(3.1415, testStrategy.doubleParam, 0.0001);
        assertFalse(testStrategy.booleanParam);
        assertEquals(TestEnum.VALUE1, testStrategy.enumParam);
    }

    @Test
    void testSetParameterWithValidEnum() throws NoSuchFieldException, IllegalAccessException {
        ParameterHandler.setParameter(testStrategy, "enumParam", "VALUE1");
        assertEquals(TestEnum.VALUE1, testStrategy.enumParam);

        ParameterHandler.setParameter(testStrategy, "enumParam", "VALUE3");
        assertEquals(TestEnum.VALUE3, testStrategy.enumParam);
    }

    @Test
    void testSetParameterWithInvalidEnum() {
        assertThrows(IllegalArgumentException.class, () ->
                ParameterHandler.setParameter(testStrategy, "enumParam", "INVALID_VALUE")
        );
    }

    @Test
    void testInitializeWithInvalidEnum() {
        class InvalidEnumStrategy extends BaseStrategy {
            @Parameter(name = "enumParam", description = "Enum parameter", value = "INVALID_VALUE")
            private TestEnum enumParam;

            public InvalidEnumStrategy(String strategyId) {
                super(strategyId);
            }

            @Override
            public void onBarClose(Bar bar) {
            }

            @Override
            public void onTick(Tick tick, Bar currentBar) {
            }
        }

        InvalidEnumStrategy invalidStrategy = new InvalidEnumStrategy("invalid");
        assertThrows(IllegalArgumentException.class, () ->
                ParameterHandler.initialize(invalidStrategy)
        );
    }

    @Test
    void testEnumCaseSensitivity() throws NoSuchFieldException, IllegalAccessException {
        assertThrows(IllegalArgumentException.class, () ->
                ParameterHandler.setParameter(testStrategy, "enumParam", "value1")
        );

        // This should work
        ParameterHandler.setParameter(testStrategy, "enumParam", "VALUE1");
        assertEquals(TestEnum.VALUE1, testStrategy.enumParam);
    }

    @Test
    void testEnumWithNullValue() {
        assertThrows(IllegalArgumentException.class, () ->
                ParameterHandler.setParameter(testStrategy, "enumParam", null)
        );
    }

    enum TestEnum {
        VALUE1, VALUE2, VALUE3
    }

    // This inner class definition is for testing purposes only
    static class TestStrategy extends BaseStrategy {
        @Parameter(name = "intParam", description = "Integer parameter", value = "10")
        private int intParam;

        @Parameter(name = "doubleParam", description = "Double parameter", value = "3.14")
        private double doubleParam;

        @Parameter(name = "stringParam", description = "String parameter", value = "default")
        private String stringParam;

        @Parameter(name = "enumParam", description = "Enum parameter", value = "VALUE2")
        private TestEnum enumParam;

        @Parameter(name = "booleanParam", description = "Boolean parameter", value = "true")
        private boolean booleanParam;

        public TestStrategy(String strategyId) {
            super(strategyId);
        }

        @Override
        public void onBarClose(Bar bar) {
            // Not used for this test
        }

        public void onTick(Tick tick, Bar currentBar) {
            // Not used for this test
        }
    }
}