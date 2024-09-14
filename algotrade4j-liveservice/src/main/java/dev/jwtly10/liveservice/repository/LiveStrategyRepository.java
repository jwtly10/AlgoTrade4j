package dev.jwtly10.liveservice.repository;

import dev.jwtly10.liveservice.model.BrokerAccount;
import dev.jwtly10.liveservice.model.LiveStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveStrategyRepository extends JpaRepository<LiveStrategy, Long> {
    List<LiveStrategy> findLiveStrategiesByHiddenIsFalse();

    List<LiveStrategy> findLiveStrategiesByHiddenIsFalseAndActiveIsTrue();

    List<LiveStrategy> findLiveStrategiesByBrokerAccountAndHiddenIsFalse(BrokerAccount brokerAccount);
}