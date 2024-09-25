package dev.jwtly10.liveapi.config;

import dev.jwtly10.liveapi.repository.LiveExecutorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreBeanConfig {
    @Bean
    public LiveExecutorRepository executorRepository() {
        return new LiveExecutorRepository();
    }
}