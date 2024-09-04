package dev.jwtly10.api.config;

import dev.jwtly10.api.service.StrategyManager;
import dev.jwtly10.core.data.DataManagerFactory;
import dev.jwtly10.core.data.DefaultDataManagerFactory;
import dev.jwtly10.core.event.AsyncEventPublisher;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.DefaultExecutorFactory;
import dev.jwtly10.core.execution.ExecutorFactory;
import dev.jwtly10.core.strategy.DefaultStrategyFactory;
import dev.jwtly10.core.strategy.StrategyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {

    @Bean
    public EventPublisher eventPublisher() {
        return new AsyncEventPublisher();
    }

    @Bean
    public StrategyManager strategyManager(EventPublisher eventPublisher) {
        return new StrategyManager(eventPublisher);
    }

    @Bean
    public StrategyFactory strategyFactory() {
        return new DefaultStrategyFactory();
    }

    @Bean
    public ExecutorFactory executorFactory() {
        return new DefaultExecutorFactory();
    }

    @Bean
    public DataManagerFactory dataManagerFactory() {
        return new DefaultDataManagerFactory();
    }
}