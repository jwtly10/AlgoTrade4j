package dev.jwtly10.liveapi.service.strategy;

import dev.jwtly10.core.event.types.LogEvent;
import dev.jwtly10.liveapi.model.LiveStrategy;
import dev.jwtly10.liveapi.model.LiveStrategyLog;
import dev.jwtly10.liveapi.repository.LiveStrategyLogRepository;
import dev.jwtly10.liveapi.repository.LiveStrategyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiveStrategyLogService {

    private final LiveStrategyLogRepository liveStrategyLogRepository;
    private final LiveStrategyRepository liveStrategyRepository;

    public LiveStrategyLogService(LiveStrategyLogRepository liveStrategyLogRepository, LiveStrategyRepository liveStrategyRepository) {
        this.liveStrategyLogRepository = liveStrategyLogRepository;
        this.liveStrategyRepository = liveStrategyRepository;
    }

    public void log(String strategyId, LogEvent.LogType level, String message) {
        LiveStrategy liveStrategy = liveStrategyRepository.findByStrategyName(strategyId).orElseThrow(() ->
                new RuntimeException("Strategy not found with id: " + strategyId)
        );

        liveStrategyLogRepository.save(LiveStrategyLog.builder()
                .liveStrategy(liveStrategy)
                .level(level.name())
                .message(message)
                .build());
    }

    public List<LiveStrategyLog> getLogs(String strategyId) {
        return liveStrategyLogRepository.findAll();
    }
}