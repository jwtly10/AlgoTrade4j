package dev.jwtly10.liveapi.repository.broker;

import dev.jwtly10.liveapi.model.broker.BrokerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrokerAccountRepository extends JpaRepository<BrokerAccount, Long> {
    Optional<BrokerAccount> findByAccountIdAndActiveIsTrue(String accountId);

    List<BrokerAccount> findByActiveIsTrue();

    List<BrokerAccount> findByActiveIsTrueOrderByIdDesc();

    void deleteByAccountId(String accountId);
}