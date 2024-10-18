package dev.jwtly10.marketdata.impl.mt5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.Number;
import dev.jwtly10.core.model.*;
import dev.jwtly10.marketdata.common.TradeDTO;
import dev.jwtly10.marketdata.common.stream.Stream;
import dev.jwtly10.marketdata.impl.mt5.models.MT5Login;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "TEST_MT5_ACCOUNT_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TEST_MT5_ACCOUNT_PASSWORD", matches = ".+")
class MT5BrokerClientIntegrationTest {

    private MT5BrokerClient client;
    private MT5Client mt5;

    private Broker TEST_BROKER = Broker.MT5_FTMO;

    private String accountId;
    private String password;

    @BeforeEach
    void setUp() {
        String apiUrl = "http://192.168.68.103:5000";
        String apiKey = "password";

        this.accountId = System.getenv("TEST_MT5_ACCOUNT_ID");
        this.password = System.getenv("TEST_MT5_ACCOUNT_PASSWORD");

        assertNotNull(accountId, "TEST_MT5_ACCOUNT_ID environment variable not set");
        assertNotNull(password, "TEST_MT5_ACCOUNT_PASSWORD environment variable not set");

        ObjectMapper objMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mt5 = new MT5Client(apiKey, apiUrl, objMapper);

        MT5Login login = new MT5Login(Integer.parseInt(accountId), password, "FTMO-Demo", "C:/Program Files/MetaTrader 5/terminal64.exe");

        // Oanda logic is outside scope of this test
        this.client = new MT5BrokerClient(mt5, null, login, null, Broker.MT5_FTMO, ZoneId.of("UTC"));
    }

    @Test
    void testInitialiseAccountDetails() throws Exception {
        mt5.initializeAccount(Integer.parseInt(accountId), password, "FTMO-Demo", "C:/Program Files/MetaTrader 5/terminal64.exe");
    }

    @Test
    void testGetAccount() throws Exception {
        Account res = client.getAccountInfo();

        assertEquals(-999999, res.getInitialBalance());
        assertNotEquals(0.0, res.getBalance());
        assertNotEquals(0.0, res.getEquity());
    }

    @Test
    void testGetTrades() throws Exception {
        List<Trade> trades = client.getAllTrades();

        assertNotEquals(0, trades.size());

        for (Trade trade : trades) {
            System.out.println(trade);
        }
    }

    @Test
    void testOpenTrade() throws Exception {
        final Instrument SYMBOL = Instrument.NAS100USD;
        TradeParameters params = new TradeParameters();
        params.setInstrument(SYMBOL);
        // This number should be the 'current' price on the mt5 charts
        Number price = new Number(20264);
        params.setEntryPrice(price);
        int stopLossTicks = 300;
        boolean isLong = false;
        params.setLong(isLong);
        Number pipValue = new Number(stopLossTicks * SYMBOL.getBrokerConfig(TEST_BROKER).getPipValue());
        Number stopLossPrice = isLong ? price.subtract(pipValue) : price.add(pipValue);
        params.setStopLoss(stopLossPrice);
        double riskRatio = 2;
        params.setRiskRatio(riskRatio);
        Number takeProfitPrice = isLong ? price.add(pipValue.multiply(BigDecimal.valueOf(riskRatio))) : price.subtract(pipValue.multiply(BigDecimal.valueOf(riskRatio)));
        params.setTakeProfit(takeProfitPrice);
        double riskPercentage = 0.01;
        params.setRiskPercentage(riskPercentage);
        double balanceToRisk = 10_000.0;
        params.setBalanceToRisk(balanceToRisk);
        params.setOpenTime(ZonedDateTime.now());


        Trade res = client.openTrade(params);
        System.out.println(res);
    }

    @Test
    void testCloseTrade() throws Exception {
        int openTradeId = 183797372;

        client.closeTrade(openTradeId);
    }

    @Test
    void testStreamTransactions() throws InterruptedException {
        Stream<List<TradeDTO>> transactionStream = client.streamTransactions();

        transactionStream.start(new Stream.StreamCallback<>() {
            @Override
            public void onData(List<TradeDTO> closedTradeDTOs) {
                closedTradeDTOs.forEach(System.out::println);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("Transaction stream completed");
            }
        });


        // Sleep for 30 seconds so we can see events come in
        Thread.sleep(60_000);
    }

}