package dev.jwtly10.core.strategy;

import dev.jwtly10.core.model.Bar;
import dev.jwtly10.core.model.Tick;
import dev.jwtly10.core.risk.RiskProfileConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParameterHandlerTest {

    private TestStrategy testStrategy;

    @BeforeEach
    void setUp() {
        testStrategy = new TestStrategy("test");
    }

    @Test
    void testValidateRunParameters() {
        TestStrategy testStrategy = new TestStrategy("test");

        // Test with valid parameters
        Map<String, String> validParams = new HashMap<>();
        validParams.put("intParam", "20");
        validParams.put("doubleParam", "6.28");
        validParams.put("stringParam", "newValue");
        validParams.put("enumParam", "VALUE3");
        validParams.put("booleanParam", "false");

        assertDoesNotThrow(() -> ParameterHandler.validateRunParameters(testStrategy, validParams));

        // Test with missing parameter
        Map<String, String> missingParams = new HashMap<>(validParams);
        missingParams.remove("intParam");
        Exception missingEx = assertThrows(IllegalArgumentException.class,
                () -> ParameterHandler.validateRunParameters(testStrategy, missingParams));
        assertTrue(missingEx.getMessage().contains("Missing required parameter: intParam"));

        // Test with extra parameter
        Map<String, String> extraParams = new HashMap<>(validParams);
        extraParams.put("extraParam", "extra");
        Exception extraEx = assertThrows(IllegalArgumentException.class,
                () -> ParameterHandler.validateRunParameters(testStrategy, extraParams));
        assertTrue(extraEx.getMessage().contains("Unexpected parameter: extraParam"));

        // Test with invalid type
        Map<String, String> invalidTypeParams = new HashMap<>(validParams);
        invalidTypeParams.put("intParam", "notAnInteger");
        Exception invalidTypeEx = assertThrows(IllegalArgumentException.class,
                () -> ParameterHandler.validateRunParameters(testStrategy, invalidTypeParams));
        assertTrue(invalidTypeEx.getMessage().contains("Invalid value for [int] parameter intParam"));

        // Test with invalid enum value
        Map<String, String> invalidEnumParams = new HashMap<>(validParams);
        invalidEnumParams.put("enumParam", "INVALID_VALUE");
        Exception invalidEnumEx = assertThrows(IllegalArgumentException.class,
                () -> ParameterHandler.validateRunParameters(testStrategy, invalidEnumParams));
        assertTrue(invalidEnumEx.getMessage().contains("Invalid value for [static final enum dev.jwtly10.core.strategy.ParameterHandlerTest$TestEnum] parameter enumParam"));
    }

    @Test
    void testValidateRunParametersEdgeCases() {
        // Test with strategy that has no parameters configured
        TestStrategyEdgeCase testStrategy = new TestStrategyEdgeCase("test");

        // Test with no parameters
        assertDoesNotThrow(() -> ParameterHandler.validateRunParameters(testStrategy, Map.of()));

        // Test with parameters
        Map<String, String> params = Map.of("param1", "value1", "param2", "value2");
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> ParameterHandler.validateRunParameters(testStrategy, params));
        assertTrue(ex.getMessage().contains("Unexpected parameter: param")); // It's a map, it can be either 1 or 2, so we cant validate the number
    }

    @Test
    void testGetParameters() throws IllegalAccessException {
        ParameterHandler.initialize(testStrategy);
        List<ParameterHandler.ParameterInfo> params = ParameterHandler.getParameters(testStrategy);

        assertParameter(params, "intParam", "10");
        assertParameter(params, "doubleParam", "3.14");
        assertParameter(params, "stringParam", "default");
        assertParameter(params, "enumParam", "VALUE2");
        assertParameter(params, "booleanParam", "true");
    }

    private void assertParameter(List<ParameterHandler.ParameterInfo> params, String name, String expectedValue) {
        assertTrue(params.stream()
                        .filter(p -> p.getName().equals(name))
                        .findFirst()
                        .map(p -> p.getValue().equals(expectedValue))
                        .orElse(false),
                "Parameter '" + name + "' should have value '" + expectedValue + "'");
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

            @Override
            public RiskProfileConfig getRiskProfileConfig() {
                return null;
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
    void testSetParameters() {
        Map<String, String> parameters = Map.of(
                "intParam", "20",
                "doubleParam", "6.28",
                "stringParam", "newValue",
                "enumParam", "VALUE3",
                "booleanParam", "false"
        );
        assertDoesNotThrow(() -> ParameterHandler.setParameters(testStrategy, parameters));
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

            @Override
            public RiskProfileConfig getRiskProfileConfig() {
                return null;
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

        @Parameter(name = "enumParam", description = "Enum parameter", value = "VALUE2", enumClass = TestEnum.class)
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

        @Override
        public RiskProfileConfig getRiskProfileConfig() {
            return null;
        }
    }

    static class TestStrategyEdgeCase extends BaseStrategy {
        public TestStrategyEdgeCase(String strategyId) {
            super(strategyId);
        }

        @Override
        public void onBarClose(Bar bar) {
            // Not used for this test
        }

        public void onTick(Tick tick, Bar currentBar) {
            // Not used for this test
        }

        @Override
        public RiskProfileConfig getRiskProfileConfig() {
            return null;
        }
    }
}