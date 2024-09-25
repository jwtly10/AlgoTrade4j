package dev.jwtly10.core.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAccountManagerTest {

    private DefaultAccountManager accountManager;
    private double currentBalance;
    private double currentEquity;
    private double initialBalance;
    private double openPositionValue;

    @BeforeEach
    void setUp() {
        initialBalance = 1000.0;
        currentBalance = 1000.0;
        currentEquity = 500.0;
        openPositionValue = -500.0; // equity - balance
        accountManager = new DefaultAccountManager(initialBalance, currentBalance, currentEquity);
    }

    @Test
    void testGetBalance() {
        assertEquals(initialBalance, accountManager.getBalance());
    }

    @Test
    void testSetBalance() {
        double newBalance = (1500.0);
        accountManager.setBalance(newBalance);
        assertEquals(newBalance, accountManager.getBalance());
    }

    @Test
    void testGetEquity() {
        assertEquals(currentEquity, accountManager.getEquity());
    }

    @Test
    void testSetEquity() {
        double newEquity = 750.0;
        accountManager.setEquity(newEquity);
        assertEquals(newEquity, accountManager.getEquity());
    }

    @Test
    void testGetInitialBalance() {
        assertEquals(initialBalance, accountManager.getInitialBalance());
    }

    @Test
    void testGetOpenPositionValue() {
        assertEquals(openPositionValue, accountManager.getOpenPositionValue());
    }

    @Test
    void testConstructor() {
        double balance = (2000.0);
        double equity = (1000.0);
        double initialBalance = (2000.0);
        DefaultAccountManager newManager = new DefaultAccountManager(initialBalance, balance, equity);

        assertEquals(balance, newManager.getBalance());
        assertEquals(equity, newManager.getEquity());
        assertEquals(initialBalance, newManager.getInitialBalance());
    }

    @Test
    void testNewConstructor() {
        double initialBalance = (2000.0);
        DefaultAccountManager newManager = new DefaultAccountManager(initialBalance);

        assertEquals(initialBalance, newManager.getBalance());
        assertEquals(initialBalance, newManager.getEquity());
        assertEquals(initialBalance, newManager.getInitialBalance());
    }

}