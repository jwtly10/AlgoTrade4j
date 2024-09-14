package dev.jwtly10.liveservice.config;

import dev.jwtly10.core.data.DataManagerFactory;
import dev.jwtly10.core.data.DefaultDataManagerFactory;
import dev.jwtly10.core.event.AsyncEventPublisher;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.core.execution.DefaultExecutorFactory;
import dev.jwtly10.core.execution.ExecutorFactory;
import dev.jwtly10.core.strategy.DefaultStrategyFactory;
import dev.jwtly10.core.strategy.StrategyFactory;
import dev.jwtly10.liveservice.repository.InMemoryExecutorRepository;
import dev.jwtly10.marketdata.oanda.OandaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfig {

    @Value("${oanda.api.key}")
    private String oandaApiKey;
    @Value("${oanda.api.url}")
    private String oandaApiUrl;


    @Bean
    public EventPublisher eventPublisher() {
        return new AsyncEventPublisher();
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

    @Bean
    public InMemoryExecutorRepository executorRepository() {
        return new InMemoryExecutorRepository();
    }

    @Bean
    public OandaClient oandaClient() {
        return new OandaClient(oandaApiUrl, oandaApiKey);
    }
}