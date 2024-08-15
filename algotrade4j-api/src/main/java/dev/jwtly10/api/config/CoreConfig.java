package dev.jwtly10.api.config;

import dev.jwtly10.api.service.OptimisationManager;
import dev.jwtly10.api.service.StrategyManager;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.optimisation.OptimisationExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {

    @Bean
    public EventPublisher eventPublisher() {
        return new EventPublisher();
    }

    @Bean
    public StrategyManager strategyManager(EventPublisher eventPublisher) {
        return new StrategyManager(eventPublisher);
    }

    @Bean
    public OptimisationManager optimisationManager(OptimisationExecutor optimisationExecutor) {
        return new OptimisationManager(optimisationExecutor);
    }

    @Bean
    public OptimisationExecutor optimisationExecutor(EventPublisher eventPublisher) {
        return new OptimisationExecutor(eventPublisher);
    }

}