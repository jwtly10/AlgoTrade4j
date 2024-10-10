package dev.jwtly10.backtestapi.config;

import dev.jwtly10.core.event.AsyncEventPublisher;
import dev.jwtly10.core.event.EventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BacktestBeanConfig {
    @Bean
    public EventPublisher eventPublisher() {
        return new AsyncEventPublisher();
    }
}