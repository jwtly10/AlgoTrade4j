package dev.jwtly10.liveapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.event.EventPublisher;
import dev.jwtly10.liveapi.repository.LiveExecutorRepository;
import dev.jwtly10.liveapi.service.event.LiveAsyncEventPublisher;
import dev.jwtly10.liveapi.service.risk.RiskManagementServiceClient;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyLogService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiveApiBeanConfig {

    @Value("${risk.management.api.url}")
    private String riskManagementApiUrl;

    @Value("${risk.management.api.key}")
    private String riskManagementApiKey;

    @Bean
    public LiveExecutorRepository executorRepository() {
        return new LiveExecutorRepository();
    }

    @Bean
    public EventPublisher eventPublisher(LiveStrategyLogService liveStrategyLogService) {
        return new LiveAsyncEventPublisher(liveStrategyLogService);
    }

    @Bean
    public RiskManagementServiceClient riskManagementServiceClient(OkHttpClient okHttpClient, ObjectMapper objectMapper) {
        return new RiskManagementServiceClient(riskManagementApiUrl, riskManagementApiKey, okHttpClient, objectMapper);
    }
}