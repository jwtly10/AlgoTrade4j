package dev.jwtly10.liveapi.repository;

import dev.jwtly10.liveapi.model.LiveStrategyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveStrategyLogRepository extends JpaRepository<LiveStrategyLog, Long> {
}