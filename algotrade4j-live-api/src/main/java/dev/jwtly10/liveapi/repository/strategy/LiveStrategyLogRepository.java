package dev.jwtly10.liveapi.repository.strategy;

import dev.jwtly10.liveapi.model.strategy.LiveStrategyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveStrategyLogRepository extends JpaRepository<LiveStrategyLog, Long> {
    List<LiveStrategyLog> findAllByLiveStrategyIdOrderByIdDesc(Long strategyId);
}