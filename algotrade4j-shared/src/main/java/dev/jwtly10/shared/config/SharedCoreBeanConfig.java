package dev.jwtly10.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jwtly10.core.data.DataManagerFactory;
import dev.jwtly10.core.data.DefaultDataManagerFactory;
import dev.jwtly10.core.execution.DefaultExecutorFactory;
import dev.jwtly10.core.execution.ExecutorFactory;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.strategy.DefaultStrategyFactory;
import dev.jwtly10.core.strategy.StrategyFactory;
import dev.jwtly10.marketdata.oanda.OandaClient;
import dev.jwtly10.shared.service.external.telegram.TelegramNotifier;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedCoreBeanConfig {

    @Value("${oanda.api.key}")
    private String oandaApiKey;

    @Value("${oanda.api.url}")
    private String oandaApiUrl;

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Number.class, new NumberDeserializer());
        module.addSerializer(Number.class, new NumberSerializer());
        mapper.registerModule(module);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public OandaClient oandaClient() {
        return new OandaClient(oandaApiUrl, oandaApiKey, objectMapper());
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
    public OkHttpClient okHttpClient() {
        return new OkHttpClient();
    }

    @Bean
    public TelegramNotifier telegramNotifier() {
        return new TelegramNotifier(okHttpClient(), telegramBotToken);
    }
}