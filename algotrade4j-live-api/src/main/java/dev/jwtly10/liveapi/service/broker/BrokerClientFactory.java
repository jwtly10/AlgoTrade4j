package dev.jwtly10.liveapi.service.broker;

import dev.jwtly10.liveapi.model.broker.BrokerAccount;
import dev.jwtly10.marketdata.common.BrokerClient;
import dev.jwtly10.marketdata.impl.mt5.Mt5BrokerClient;
import dev.jwtly10.marketdata.impl.mt5.Mt5Client;
import dev.jwtly10.marketdata.impl.mt5.models.Mt5Login;
import dev.jwtly10.marketdata.impl.oanda.OandaBrokerClient;
import dev.jwtly10.marketdata.impl.oanda.OandaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BrokerClientFactory {

    private final OandaClient oandaClient;
    private final Mt5Client mt5Client;

    @Value("${default.oanda.account.id}")
    private String defaultOandaAccountId;

    public BrokerClientFactory(OandaClient oandaClient, Mt5Client mt5Client) {
        this.oandaClient = oandaClient;
        this.mt5Client = mt5Client;
    }

    public BrokerClient createBrokerClientFromBrokerConfig(BrokerAccount brokerConfig) {
        return switch (brokerConfig.getBrokerType()) {
            case OANDA -> new OandaBrokerClient(oandaClient, brokerConfig.getAccountId());
            case MT5_FTMO -> new Mt5BrokerClient(mt5Client, oandaClient, new Mt5Login(
                    Integer.parseInt(brokerConfig.getAccountId()),
                    brokerConfig.getMt5Credentials().getPassword(),
                    brokerConfig.getMt5Credentials().getServer(),
                    brokerConfig.getMt5Credentials().getPath()
            ), defaultOandaAccountId, brokerConfig.getBrokerType());
            default -> throw new RuntimeException("Broker not supported yet: " + brokerConfig.getBrokerType());
        };
    }
}