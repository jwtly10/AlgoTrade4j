package dev.jwtly10.liveservice.config;

import dev.jwtly10.liveservice.repository.InMemoryExecutorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreBeanConfig {
    @Bean
    public InMemoryExecutorRepository executorRepository() {
        return new InMemoryExecutorRepository();
    }
}