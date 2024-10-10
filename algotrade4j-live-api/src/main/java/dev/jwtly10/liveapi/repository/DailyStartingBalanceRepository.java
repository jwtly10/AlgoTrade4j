package dev.jwtly10.liveapi.repository;

import dev.jwtly10.liveapi.model.DailyStartingBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyStartingBalanceRepository extends JpaRepository<DailyStartingBalance, Long> {
    List<DailyStartingBalance> findAllByLiveStrategyId(long liveStrategyId);
}