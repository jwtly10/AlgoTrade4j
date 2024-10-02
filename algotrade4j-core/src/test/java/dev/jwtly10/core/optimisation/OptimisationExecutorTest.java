package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.data.DataManagerFactory;
import dev.jwtly10.core.data.DataProvider;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.ExecutorFactory;
import dev.jwtly10.core.strategy.StrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

class OptimisationExecutorTest {

    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private DataProvider dataProvider;
    @Mock
    private Consumer<OptimisationRunResult> resultCallback;
    @Mock
    private Consumer<OptimisationProgress> progressCallback;
    @Mock
    private StrategyFactory strategyFactory;
    @Mock
    private ExecutorFactory executorFactory;
    @Mock
    private DataManagerFactory dataManagerFactory;

    private OptimisationExecutor optimisationExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        optimisationExecutor = new OptimisationExecutor(
                eventPublisher, dataProvider, resultCallback, progressCallback,
                strategyFactory, executorFactory, dataManagerFactory
        );
    }

    @Test
    void generateParameterCombinations() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("default_value", "SMA1", "10", "60", "5", true, null),
                new ParameterRange("default_value", "SMA2", "20", "30", "5", true, null)
        );

        // This will generate
        // SMA1=10, SMA2=20
        // SMA1=10, SMA2=25
        // SMA1=10, SMA2=30
        // SMA1=15, SMA2=20
        // SMA1=15, SMA2=25
        // SMA1=15, SMA2=30
        // SMA1=20, SMA2=20
        // SMA1=20, SMA2=25
        // SMA1=20, SMA2=30
        // SMA1=25, SMA2=20
        // SMA1=25, SMA2=25
        // SMA1=25, SMA2=30
        // Which is every possible combination of the parameters
        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);

        assertEquals(33, combinations.size());
    }

    @Test
    void generateParameterCombinationsOneStep() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("default_value", "SMA1", "10", "10", "10", true, null),
                new ParameterRange("default_value", "SMA2", "10", "10", "10", true, null)
        );

        // This will generate
        // SMA1=10, SMA2=10
        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);
        assertEquals(1, combinations.size());
    }

    @Test
    void generateParameterCombinationsInvalidParameterRange() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("default_value", "SMA1", "10", "10", "10", true, null),
                new ParameterRange("default_value", "SMA2", "10", "10", "10", true, null)
        );

        // This will generate
        // SMA1=10, SMA2=10
        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);
        assertEquals(1, combinations.size());
    }

    @Test
    void testParameterRangeWithDecimalValues() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("default_value", "Threshold", "0.10", "0.30", "0.10", true, null)
        );

        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);

        System.out.println(combinations);

        assertEquals(3, combinations.size());
        assertEquals(String.valueOf(0.10), combinations.getFirst().get("Threshold"));
        assertEquals(String.valueOf(0.20), combinations.get(1).get("Threshold"));
        assertEquals(String.valueOf(0.30), combinations.getLast().get("Threshold"));
    }

    @Test
    void testParameterRangeWithEnums() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("default_value", "Threshold", "0.10", "0.30", "0.10", true, "0.10,0.20,0.30")
        );
    }

    @Test
    void generateParameterCombinationsLargeSet() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("10", "SMA1", "10.00000", "30.00000", "10.00000", true, null),
                new ParameterRange("20", "SMA2", "20.00000", "40.00000", "10.00000", true, null),
                new ParameterRange("30", "SMA3", "30.00000", "50.00000", "10.00000", true, null),
                new ParameterRange("40", "SMA4", "40.00000", "60.00000", "10.00000", true, null),
                new ParameterRange("50", "SMA5", "50.00000", "70.00000", "10.00000", false, null),
                new ParameterRange("1.5", "Threshold", "1.00000", "2.00000", "0.50000", true, null)
        );

        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);

        // Calculate expected number of combinations
        // SMA1: 3 values (10.00000, 20.00000, 30.00000)
        // SMA2: 3 values (20.00000, 30.00000, 40.00000)
        // SMA3: 3 values (30.00000, 40.00000, 50.00000)
        // SMA4: 3 values (40.00000, 50.00000, 60.00000)
        // SMA5: 1 value (50, default)
        // Threshold: 3 values (1.00000, 1.50000, 2.00000)
        // Total: 3 * 3 * 3 * 3 * 1 * 3 = 243 combinations

        int expectedCombinations = 243;
        assertEquals(expectedCombinations, combinations.size());

        // First combination
        Map<String, String> firstCombination = combinations.getFirst();
        assertEquals(String.valueOf(10.00000), firstCombination.get("SMA1"));
        assertEquals(String.valueOf(20.0000), firstCombination.get("SMA2"));
        assertEquals(String.valueOf(30.00000), firstCombination.get("SMA3"));
        assertEquals(String.valueOf(40.00000), firstCombination.get("SMA4"));
        assertEquals(String.valueOf(50), firstCombination.get("SMA5"));  // Default value
        assertEquals(String.valueOf(1.00000), firstCombination.get("Threshold"));

        // Last combination
        Map<String, String> lastCombination = combinations.getLast();
        assertEquals(String.valueOf(30.00000), lastCombination.get("SMA1"));
        assertEquals(String.valueOf(40.00000), lastCombination.get("SMA2"));
        assertEquals(String.valueOf(50.00000), lastCombination.get("SMA3"));
        assertEquals(String.valueOf(60.00000), lastCombination.get("SMA4"));
        assertEquals(String.valueOf(50), lastCombination.get("SMA5"));  // Default value
        assertEquals(String.valueOf(2.00000), lastCombination.get("Threshold"));

        // SMA5 always has its default value
        for (Map<String, String> combination : combinations) {
            assertEquals(String.valueOf(50), combination.get("SMA5"));
        }
    }

    @Test
    void generateParameterCombinationsWithSelectedAndUnselected() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("15", "SMA1", "10", "20", "10", true, null),
                new ParameterRange("25", "SMA2", "20", "30", "10", false, null),
                new ParameterRange("35", "SMA3", "30", "40", "10", true, null)
        );

        // This will generate:
        // SMA1=10, SMA2=25, SMA3=30
        // SMA1=10, SMA2=25, SMA3=40
        // SMA1=20, SMA2=25, SMA3=30
        // SMA1=20, SMA2=25, SMA3=40
        // SMA2 uses its default value (25) in all combinations as it was NOT selected for optimisation
        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);

        assertEquals(4, combinations.size());

        // Check the first combination
        Map<String, String> firstCombination = combinations.getFirst();
        assertEquals(String.valueOf(10.00000), firstCombination.get("SMA1"));
        assertEquals(String.valueOf(25), firstCombination.get("SMA2"));  // Default value
        assertEquals(String.valueOf(30.00000), firstCombination.get("SMA3"));

        // Check the last combination
        Map<String, String> lastCombination = combinations.getLast();
        assertEquals(String.valueOf(20.00000), lastCombination.get("SMA1"));
        assertEquals(String.valueOf(25), lastCombination.get("SMA2"));  // Default value
        assertEquals(String.valueOf(40.00000), lastCombination.get("SMA3"));

        // Check that all combinations include SMA2 with its default value
        for (Map<String, String> combination : combinations) {
            assertTrue(combination.containsKey("SMA2"));
            assertEquals(String.valueOf(25), combination.get("SMA2"));
        }
    }

    @Test
    void generateParameterCombinationsForStringsAndEnums() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("AGGRESSIVE", "RISK_PROFILE", "10", "30", "10", true, "AGGRESSIVE,MODERATE,CONSERVATIVE"),
                new ParameterRange("name1", "NAME_OF_STRAT", "10", "30", "10", true, "name1, name2"),
                new ParameterRange("josh", "NAME_OF_RUNNER", "10", "30", "10", true, "josh")
        );

        // This will generate:
        // RISK_PROFILE=AGGRESSIVE, NAME_OF_STRAT=name1, NAME_OF_RUNNER=josh
        // RISK_PROFILE=AGGRESSIVE, NAME_OF_STRAT=name2, NAME_OF_RUNNER=josh
        // RISK_PROFILE=MODERATE, NAME_OF_STRAT=name1, NAME_OF_RUNNER=josh
        // RISK_PROFILE=MODERATE, NAME_OF_STRAT=name2, NAME_OF_RUNNER=josh
        // RISK_PROFILE=CONSERVATIVE, NAME_OF_STRAT=name1, NAME_OF_RUNNER=josh
        // RISK_PROFILE=CONSERVATIVE, NAME_OF_STRAT=name2, NAME_OF_RUNNER=josh
        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);
        // Print out the combinations
        for (Map<String, String> combination : combinations) {
            System.out.println(combination);
        }

        assertEquals(6, combinations.size());
    }

    @Test
    void generateParameterCombinationsForStringsAndEnumsEdgeCase() {
        // NB:
        // This generation logic is impartial to types.
        // If there is no string list defined, it will generate the range as if it were a number
        // Then the executor will validate the values and throw accordingly
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("josh", "NAME_OF_RUNNER", "10", "30", "10", true, null)
        );
        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);

        assertEquals(3, combinations.size());

        // Print out the combinations
        for (Map<String, String> combination : combinations) {
            System.out.println(combination);
        }
    }

    @Test
    void testOnStrategyFailure() {
        String strategyId = "testStrategy";
        Exception testException = new RuntimeException("Test error");

        optimisationExecutor.onStrategyFailure(strategyId, testException);

        verify(resultCallback).accept(argThat(result ->
                result.getStrategyId().equals(strategyId) &&
                        result.getOutput().isFailed() &&
                        result.getOutput().getReason().equals("Test error")
        ));
    }

    @Test
    void testExecuteTaskWithNoParameterCombinations() {
        OptimisationConfig config = new OptimisationConfig();
        config.setParameterRanges(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> optimisationExecutor.executeTask(config));
    }

    @Test
    void testExecuteTaskWithTooManyParameterCombinations() {
        OptimisationConfig config = new OptimisationConfig();
        config.setParameterRanges(Arrays.asList(
                new ParameterRange("val", "param1", "1", "100", "1", true, null),
                new ParameterRange("val", "param2", "1", "100", "1", true, null)
        ));

        assertThrows(RuntimeException.class, () -> optimisationExecutor.executeTask(config));
    }


}