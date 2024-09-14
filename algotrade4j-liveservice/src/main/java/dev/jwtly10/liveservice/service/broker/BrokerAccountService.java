package dev.jwtly10.liveservice.service.broker;

import dev.jwtly10.liveservice.model.BrokerAccount;
import dev.jwtly10.liveservice.model.LiveStrategy;
import dev.jwtly10.liveservice.repository.BrokerAccountRepository;
import dev.jwtly10.liveservice.repository.LiveStrategyRepository;
import dev.jwtly10.marketdata.common.Broker;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrokerAccountService {

    private final BrokerAccountRepository brokerAccountRepository;
    private final LiveStrategyRepository liveStrategyRepository;


    public BrokerAccountService(BrokerAccountRepository brokerAccountRepository, LiveStrategyRepository liveStrategyRepository) {
        this.brokerAccountRepository = brokerAccountRepository;
        this.liveStrategyRepository = liveStrategyRepository;
    }

    public List<Broker> getBrokers() {
        return List.of(Broker.values());
    }

    public void validateAccountId(String accountId) {
        brokerAccountRepository.findByAccountIdAndActiveIsTrue(accountId)
                .orElseThrow(() -> new RuntimeException("Account ID '" + accountId + "' not found"));
    }

    public BrokerAccount getBrokerAccount(String accountId) {
        return brokerAccountRepository.findByAccountIdAndActiveIsTrue(accountId)
                .orElseThrow(() -> new RuntimeException("Account ID '" + accountId + "' not found"));
    }

    public BrokerAccount createBrokerAccount(BrokerAccount broker) {
        BrokerAccount existingAccount = brokerAccountRepository.findByAccountIdAndActiveIsTrue(broker.getAccountId())
                .orElse(null);

        if (existingAccount != null) {
            throw new RuntimeException("Account ID '" + broker.getAccountId() + "' already exists");
        }

        broker.setActive(true);

        return brokerAccountRepository.save(broker);
    }

    public BrokerAccount updateBrokerAccount(String accountId, BrokerAccount broker) {
        BrokerAccount foundAccount = brokerAccountRepository.findByAccountIdAndActiveIsTrue(accountId)
                .orElseThrow(() -> new RuntimeException("Account ID '" + accountId + "' not found"));

        // TODO: Validate the account id passed in by making external API call if possible
        foundAccount.setAccountId(broker.getAccountId());
        foundAccount.setBrokerName(broker.getBrokerName());
        foundAccount.setBrokerType(broker.getBrokerType());
        foundAccount.setInitialBalance(broker.getInitialBalance());

        foundAccount.setActive(true);

        return brokerAccountRepository.save(foundAccount);
    }

    public List<BrokerAccount> getAccounts() {
        return brokerAccountRepository.findByActiveIsTrue();
    }

    @Transactional
    public void deleteBrokerAccount(String accountId) {
        BrokerAccount brokerAccount = brokerAccountRepository.findByAccountIdAndActiveIsTrue(accountId)
                .orElseThrow(() -> new RuntimeException("Account ID '" + accountId + "' not found"));

        List<LiveStrategy> stratsInUse = liveStrategyRepository.findLiveStrategiesByBrokerAccountAndHiddenIsFalse(brokerAccount);

        if (!stratsInUse.isEmpty()) {
            List<String> strategyNames = stratsInUse.stream()
                    .map(LiveStrategy::getStrategyName)
                    .toList();

            throw new RuntimeException("Account ID '" + accountId + "' is still in use by the following strategies: " + strategyNames);
        }

        brokerAccount.setActive(false);
        brokerAccountRepository.save(brokerAccount);
    }
}