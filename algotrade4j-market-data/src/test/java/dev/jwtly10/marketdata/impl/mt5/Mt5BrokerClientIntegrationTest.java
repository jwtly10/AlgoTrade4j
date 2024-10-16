package dev.jwtly10.marketdata.impl.mt5;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "TEST_MT5_ACCOUNT_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "TEST_MT5_ACCOUNT_PASSWORD", matches = ".+")
class Mt5BrokerClientIntegrationTest {

    private Mt5BrokerClient client;
    private Mt5Client mt5;

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


        ObjectMapper objMapper = new ObjectMapper();
        mt5 = new Mt5Client(apiKey, apiUrl, objMapper);

        // Oanda logic is outside scope of this test
        this.client = new Mt5BrokerClient(mt5, null, "1510057641", null);
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
    void testOpenTrade() {
        // TODO
    }

    @Test
    void testCloseTrade() {
        // TODO
    }

    @Test
    void testStreamTransactions() {
    }
}