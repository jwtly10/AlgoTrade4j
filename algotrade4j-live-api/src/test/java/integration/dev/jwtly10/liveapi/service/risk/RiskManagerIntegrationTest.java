package integration.dev.jwtly10.liveapi.service.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.jwtly10.core.risk.DailyEquity;
import dev.jwtly10.liveapi.service.risk.LiveRiskManager;
import dev.jwtly10.liveapi.service.risk.RiskManagementServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RiskManagerIntegrationTest {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private LiveRiskManager riskManager;

    @BeforeEach
    void setUp() {
        String apiKey = "testing";
        RiskManagementServiceClient client = new RiskManagementServiceClient("http://localhost:8001/api/v1", apiKey, objectMapper);
        riskManager = new LiveRiskManager(client, "101-004-24749363-003");
    }

    @Test
    void testGetDailyEquityForOandaAccount() throws Exception {
        DailyEquity dailyEquity = riskManager.getCurrentDayStartingEquity().orElseThrow();
        assertNotNull(dailyEquity);
        assertEquals("101-004-24749363-003", dailyEquity.accountId());

        System.out.println("Daily equity for account " + dailyEquity.accountId() + " is: " + dailyEquity.lastEquity() + " at " + dailyEquity.updatedAt());
     }
}