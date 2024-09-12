package dev.jwtly10.liveservice.service.biz;

import dev.jwtly10.liveservice.model.BrokerAccount;
import dev.jwtly10.liveservice.repository.BrokerAccountRepository;
import dev.jwtly10.marketdata.common.Broker;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrokerAccountService {

    private final BrokerAccountRepository brokerAccountRepository;

    public BrokerAccountService(BrokerAccountRepository brokerAccountRepository) {
        this.brokerAccountRepository = brokerAccountRepository;
    }

    public List<Broker> getBrokers() {
        return List.of(Broker.values());
    }

    public List<String> getAccountIds() {
        return brokerAccountRepository.findAll().stream()
                .map(BrokerAccount::getAccountId)
                .toList();
    }

    public void validateAccountId(String accountId) {
        brokerAccountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Account ID '" + accountId + "' not found"));
    }

    public BrokerAccount getBrokerAccount(String accountId) {
        return brokerAccountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Account ID '" + accountId + "' not found"));
    }

    public BrokerAccount createBrokerAccount(BrokerAccount broker) {
        BrokerAccount existingAccount = brokerAccountRepository.findByAccountId(broker.getAccountId())
                .orElse(null);

        if (existingAccount != null) {
            throw new RuntimeException("Account ID '" + broker.getAccountId() + "' already exists");
        }

        return brokerAccountRepository.save(broker);
    }

    public BrokerAccount updateBrokerAccount(String accountId, BrokerAccount broker) {
        BrokerAccount foundAccount = brokerAccountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Account ID '" + accountId + "' not found"));

        // TODO: Validate the account id passed in by making external API call if possible
        foundAccount.setAccountId(broker.getAccountId());
        foundAccount.setBrokerName(broker.getBrokerName());
        foundAccount.setBrokerType(broker.getBrokerType());
        foundAccount.setInitialBalance(broker.getInitialBalance());

        return brokerAccountRepository.save(foundAccount);
    }

    public List<BrokerAccount> getAccounts() {
        return brokerAccountRepository.findAll();
    }

    @Transactional
    public void deleteBrokerAccount(String accountId) {
        brokerAccountRepository.deleteByAccountId(accountId);
    }
}