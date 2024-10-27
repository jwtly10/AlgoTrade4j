package dev.jwtly10.liveapi.config;

import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.liveapi.repository.LiveExecutorRepository;
import dev.jwtly10.liveapi.service.event.LiveAsyncEventPublisher;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyLogService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveApiBeanConfig {
    @Bean
    public LiveExecutorRepository executorRepository() {
        return new LiveExecutorRepository();
    }

    @Bean
    public EventPublisher eventPublisher(LiveStrategyLogService liveStrategyLogService) {
        return new LiveAsyncEventPublisher(liveStrategyLogService);
    }
}