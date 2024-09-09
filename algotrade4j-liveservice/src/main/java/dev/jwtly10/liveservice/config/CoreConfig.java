package dev.jwtly10.liveservice.config;

import dev.jwtly10.core.data.DataManagerFactory;
import dev.jwtly10.core.data.DefaultDataManagerFactory;
import dev.jwtly10.core.event.AsyncEventPublisher;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.DefaultExecutorFactory;
import dev.jwtly10.core.execution.ExecutorFactory;
import dev.jwtly10.core.strategy.DefaultStrategyFactory;
import dev.jwtly10.core.strategy.StrategyFactory;
import dev.jwtly10.liveservice.service.LiveStrategyManager;
import dev.jwtly10.liveservice.service.LiveStrategyWSHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {

    @Bean
    public EventPublisher eventPublisher() {
        return new AsyncEventPublisher();
    }

    @Bean
    public StrategyFactory strategyFactory() {
        return new DefaultStrategyFactory();
    }

    @Bean
    public LiveStrategyManager liveStrategyManager(EventPublisher eventPublisher, LiveStrategyWSHandler liveStrategyWSHandler) {
        return new LiveStrategyManager(liveStrategyWSHandler, eventPublisher);
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