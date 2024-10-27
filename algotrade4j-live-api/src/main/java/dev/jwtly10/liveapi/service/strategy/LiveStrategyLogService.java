package dev.jwtly10.liveapi.service.strategy;

import dev.jwtly10.core.event.types.LogEvent;
import dev.jwtly10.liveapi.model.strategy.LiveStrategy;
import dev.jwtly10.liveapi.model.strategy.LiveStrategyLog;
import dev.jwtly10.liveapi.repository.strategy.LiveStrategyLogRepository;
import dev.jwtly10.liveapi.repository.strategy.LiveStrategyRepository;
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

    public void log(String strategyNameId, LogEvent.LogType level, String message) {
        LiveStrategy liveStrategy = liveStrategyRepository.findByStrategyName(strategyNameId).orElseThrow(() ->
                new RuntimeException("Strategy not found with id: " + strategyNameId)
        );

        liveStrategyLogRepository.save(LiveStrategyLog.builder()
                .liveStrategyId(liveStrategy.getId())
                .level(level.name())
                .message(message)
                .build());
    }

    public List<LiveStrategyLog> getLogs(Long strategyId) {
        return liveStrategyLogRepository.findAllByLiveStrategyIdOrderByIdDesc(strategyId);
    }

    public List<LiveStrategyLog> getLogs(String strategyName) {
        LiveStrategy liveStrategy = liveStrategyRepository.findByStrategyName(strategyName).orElseThrow(() ->
                new RuntimeException("Strategy not found with name: " + strategyName)
        );

        return getLogs(liveStrategy.getId());
    }

    public List<LiveStrategyLog> getAllLiveStrategyLogs() {
        return liveStrategyLogRepository.findAll();
    }
}