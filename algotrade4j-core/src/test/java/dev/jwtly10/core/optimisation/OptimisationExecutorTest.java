package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptimisationExecutorTest {

    @Mock
    private EventPublisher eventPublisher;

    private OptimisationExecutor optimisationExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        optimisationExecutor = new OptimisationExecutor(eventPublisher);
    }

    @Test
    void generateParameterCombinations() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("SMA1", "10", "60", "5"),
                new ParameterRange("SMA2", "20", "30", "5")
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
    void testParameterRangeWithDecimalValues() {
        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("Threshold", "0.10", "0.30", "0.10")
        );

        List<Map<String, String>> combinations = optimisationExecutor.generateParameterCombinations(parameterRanges);

        assertEquals(3, combinations.size());
        System.out.println(combinations);
        assertEquals(new Number("0.10").toString(), combinations.getFirst().get("Threshold"));
        assertEquals(new Number("0.20").toString(), combinations.get(1).get("Threshold"));
        assertEquals(new Number("0.30").toString(), combinations.getLast().get("Threshold"));
    }


    // This test was just for testing purposes. It is not a real test.
    // TODO: Refactor the class to use factories to make testing stronger
    @Test
    void runOptimisation() {

        EventPublisher eventPublisher = new EventPublisher();
        OptimisationExecutor optimisationExecutor = new OptimisationExecutor(eventPublisher);

        OptimisationConfig config = new OptimisationConfig();
        config.setSymbol("NAS100USD");

        List<ParameterRange> parameterRanges = List.of(
                new ParameterRange("SMA1", "10", "100", "5"),
                new ParameterRange("SMA2", "20", "60", "5")
        );

        config.setParameterRanges(parameterRanges);

        try {
            optimisationExecutor.runOptimisation(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}