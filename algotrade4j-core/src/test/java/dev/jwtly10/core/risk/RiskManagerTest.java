package dev.jwtly10.core.risk;

import dev.jwtly10.core.account.AccountManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RiskManagerTest {

    @Mock
    private AccountManager accountManager;

    private RiskManager riskManager;
    private RiskProfileConfig config;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        config = RiskProfileConfig.builder()
                .maxDailyLoss(100.0)
                .accountLossLimit(500.0)
                .safetyBuffer(50.0)
                .profitTarget(1000.0)
                .build()
        ;

        when(accountManager.getBalance()).thenReturn(1000.0);
        when(accountManager.getInitialBalance()).thenReturn(1000.0);

        riskManager = new RiskManager(config, accountManager,
                ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1) // To force triggering a 'new day' which is where this logic is required
        );

        when(accountManager.getEquity()).thenReturn(1000.0);
        riskManager.assessRisk(ZonedDateTime.now(ZoneId.of("UTC"))); // Initial assessment
    }

    @Test
    void testAssessRisk_NoViolation() {
        when(accountManager.getEquity()).thenReturn(1000.0);
        RiskStatus status = riskManager.assessRisk(ZonedDateTime.now(ZoneId.of("UTC")));
        assertFalse(status.isRiskViolated());
        assertNull(status.getReason());
    }

    @Test
    void testAssessRisk_DailyLossExceeded() {
        when(accountManager.getEquity()).thenReturn(850.0);
        RiskStatus status = riskManager.assessRisk(ZonedDateTime.now(ZoneId.of("UTC")));
        assertTrue(status.isRiskViolated());
        assertEquals("Daily loss limit of 100.0 exceeded. Current daily loss: 150.0", status.getReason());
    }

    @Test
    void testAssessRisk_AccountLossLimitApproached() {
        when(accountManager.getEquity()).thenReturn(500.0);
        RiskStatus status = riskManager.assessRisk(ZonedDateTime.now(ZoneId.of("UTC")));
        assertTrue(status.isRiskViolated());
        assertEquals("Daily loss limit of 100.0 exceeded. Current daily loss: 500.0", status.getReason());
    }

    @Test
    void testAssessRisk_ProfitTargetReached() {
        when(accountManager.getEquity()).thenReturn(2100.0);
        RiskStatus status = riskManager.assessRisk(ZonedDateTime.now(ZoneId.of("UTC")));
        assertTrue(status.isRiskViolated());
        assertEquals("Profit target of 1000.0 reached. Current profit: 1100.0", status.getReason());
    }

    @Test
    void testCanTrade_NoViolation() {
        when(accountManager.getEquity()).thenReturn(1000.0);
        RiskStatus status = riskManager.canTrade();
        assertFalse(status.isRiskViolated());
        assertNull(status.getReason());
    }

    @Test
    void testCanTrade_Violation() {
        when(accountManager.getEquity()).thenReturn(850.0);
        RiskStatus status = riskManager.canTrade();
        assertTrue(status.isRiskViolated());
        assertEquals("Daily loss limit of 100.0 exceeded. Current daily loss: 150.0", status.getReason());
    }
}