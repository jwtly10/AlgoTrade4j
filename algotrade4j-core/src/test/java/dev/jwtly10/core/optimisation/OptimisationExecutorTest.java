package dev.jwtly10.core.optimisation;

import dev.jwtly10.core.event.EventPublisher;
import org.junit.jupiter.api.Test;

class OptimisationExecutorTest {

    @Test
    void runOptimisation() {

        EventPublisher eventPublisher = new EventPublisher();
        OptimisationExecutor optimisationExecutor = new OptimisationExecutor(eventPublisher);

        OptimisationConfig config = new OptimisationConfig();
        config.setSymbol("NAS100USD");

        ParameterRange parameterRange = new ParameterRange();

        try {
            optimisationExecutor.runOptimisation(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}