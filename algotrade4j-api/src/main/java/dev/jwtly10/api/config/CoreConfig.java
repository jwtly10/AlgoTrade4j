package dev.jwtly10.api.config;

import dev.jwtly10.api.service.StrategyManager;
import dev.jwtly10.core.event.AsyncEventPublisher;
import dev.jwtly10.core.event.EventPublisher;
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
}