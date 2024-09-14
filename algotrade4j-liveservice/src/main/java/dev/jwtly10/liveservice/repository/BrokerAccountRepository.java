package dev.jwtly10.liveservice.repository;

import dev.jwtly10.liveservice.model.BrokerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrokerAccountRepository extends JpaRepository<BrokerAccount, Long> {
    Optional<BrokerAccount> findByAccountIdAndActiveIsTrue(String accountId);

    List<BrokerAccount> findByActiveIsTrue();

    void deleteByAccountId(String accountId);
}