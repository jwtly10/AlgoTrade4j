package dev.jwtly10.liveapi.repository;

import dev.jwtly10.liveapi.model.broker.BrokerAccount;
import dev.jwtly10.liveapi.model.strategy.LiveStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiveStrategyRepository extends JpaRepository<LiveStrategy, Long> {
    List<LiveStrategy> findLiveStrategiesByHiddenIsFalse();

    List<LiveStrategy> findLiveStrategiesByHiddenIsFalseAndActiveIsTrue();

    List<LiveStrategy> findLiveStrategiesByBrokerAccountAndHiddenIsFalse(BrokerAccount brokerAccount);

    Optional<LiveStrategy> findByStrategyName(String strategyName);

    Optional<LiveStrategy> findByIdAndActiveIsTrue(Long id);

    Optional<LiveStrategy> findLiveStrategyByBrokerAccountAndHiddenIsFalseAndActiveIsTrue(BrokerAccount foundAccount);
}