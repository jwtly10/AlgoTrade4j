package dev.jwtly10.liveapi.service.broker;

import dev.jwtly10.core.model.Broker;
import dev.jwtly10.liveapi.model.broker.BrokerAccount;
import dev.jwtly10.liveapi.model.broker.Timezone;
import dev.jwtly10.liveapi.model.dto.TimezoneDTO;
import dev.jwtly10.liveapi.model.strategy.LiveStrategy;
import dev.jwtly10.liveapi.repository.broker.BrokerAccountRepository;
import dev.jwtly10.liveapi.repository.strategy.LiveStrategyRepository;
import dev.jwtly10.shared.auth.utils.SecurityUtils;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import dev.jwtly10.shared.tracking.TrackingService;
import dev.jwtly10.shared.tracking.UserAction;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BrokerAccountService {

    private final TrackingService trackingService;

    private final BrokerAccountRepository brokerAccountRepository;
    private final LiveStrategyRepository liveStrategyRepository;


    public BrokerAccountService(TrackingService trackingService, BrokerAccountRepository brokerAccountRepository, LiveStrategyRepository liveStrategyRepository) {
        this.trackingService = trackingService;
        this.brokerAccountRepository = brokerAccountRepository;
        this.liveStrategyRepository = liveStrategyRepository;
    }

    public List<Broker> getBrokers() {
        return List.of(Broker.values());
    }

    public List<TimezoneDTO> getTimezones() {
        return Stream.of(Timezone.values())
                .map(tz -> new TimezoneDTO(tz.name(), tz.getZoneId().toString()))
                .collect(Collectors.toList());
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
        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.BROKER_ACCOUNT_CREATE,
                Map.of(
                        "broker", broker
                )
        );

        BrokerAccount existingAccount = brokerAccountRepository.findByAccountIdAndActiveIsTrue(broker.getAccountId())
                .orElse(null);

        if (existingAccount != null) {
            throw new ApiException("Account ID '" + broker.getAccountId() + "' already exists", ErrorType.BAD_REQUEST);
        }

        broker.setActive(true);

        if (broker.getMt5Credentials() != null) {
            broker.getMt5Credentials().setBrokerAccount(broker);
        }

        return brokerAccountRepository.save(broker);
    }

    public BrokerAccount updateBrokerAccount(String accountId, BrokerAccount broker) {
        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.BROKER_ACCOUNT_EDIT,
                Map.of(
                        "broker", broker
                )
        );

        BrokerAccount foundAccount = brokerAccountRepository.findByAccountIdAndActiveIsTrue(accountId)
                .orElseThrow(() -> new ApiException("Account ID '" + accountId + "' not found", ErrorType.NOT_FOUND));

        LiveStrategy liveStrategy = liveStrategyRepository.findLiveStrategyByBrokerAccountAndHiddenIsFalseAndActiveIsTrue(foundAccount)
                .orElse(null);

        if (liveStrategy != null) {
            throw new ApiException("Account ID '" + accountId + "' is still in use by the strategy: " + liveStrategy.getStrategyName(), ErrorType.BAD_REQUEST);
        }

        // TODO: Validate the account id passed in by making external API call if possible
        foundAccount.setAccountId(broker.getAccountId().trim());
        foundAccount.setBrokerName(broker.getBrokerName());
        foundAccount.setBrokerType(broker.getBrokerType());
        foundAccount.setInitialBalance(broker.getInitialBalance());

        if (broker.getMt5Credentials() != null) {
            foundAccount.setMt5Credentials(broker.getMt5Credentials());
        }

        foundAccount.setActive(true);

        return brokerAccountRepository.save(foundAccount);
    }

    public List<BrokerAccount> getAccounts() {
        return brokerAccountRepository.findByActiveIsTrue();
    }

    @Transactional
    public void deleteBrokerAccount(String accountId) {
        trackingService.track(
                SecurityUtils.getCurrentUserId(),
                UserAction.BROKER_ACCOUNT_TOGGLE,
                Map.of("accountId", accountId)
        );

        BrokerAccount brokerAccount = brokerAccountRepository.findByAccountIdAndActiveIsTrue(accountId)
                .orElseThrow(() -> new ApiException("Account ID '" + accountId + "' not found", ErrorType.NOT_FOUND));

        List<LiveStrategy> stratsInUse = liveStrategyRepository.findLiveStrategiesByBrokerAccountAndHiddenIsFalse(brokerAccount);

        if (!stratsInUse.isEmpty()) {
            List<String> strategyNames = stratsInUse.stream()
                    .map(LiveStrategy::getStrategyName)
                    .toList();

            throw new ApiException("Account ID '" + accountId + "' is still in use by the following strategies: " + strategyNames, ErrorType.BAD_REQUEST);
        }

        brokerAccount.setActive(false);
        brokerAccountRepository.save(brokerAccount);
    }
}