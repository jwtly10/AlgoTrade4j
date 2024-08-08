package dev.jwtly10.core.account;

import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAccountManagerTest {

    private DefaultAccountManager accountManager;
    private Number currentBalance;
    private Number currentEquity;
    private Number initialBalance;
    private Number openPositionValue;

    @BeforeEach
    void setUp() {
        initialBalance = new Number(1000.0);
        currentBalance = new Number(1000.0);
        currentEquity = new Number(500.0);
        openPositionValue = new Number(-500.0); // equity - balance
        accountManager = new DefaultAccountManager(initialBalance, currentBalance, currentEquity);
    }

    @Test
    void testGetBalance() {
        assertEquals(initialBalance, accountManager.getBalance());
    }

    @Test
    void testSetBalance() {
        Number newBalance = new Number(1500.0);
        accountManager.setBalance(newBalance);
        assertEquals(newBalance, accountManager.getBalance());
    }

    @Test
    void testGetEquity() {
        assertEquals(currentEquity, accountManager.getEquity());
    }

    @Test
    void testSetEquity() {
        Number newEquity = new Number(750.0);
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
        Number balance = new Number(2000.0);
        Number equity = new Number(1000.0);
        Number initialBalance = new Number(2000.0);
        DefaultAccountManager newManager = new DefaultAccountManager(initialBalance, balance, equity);

        assertEquals(balance, newManager.getBalance());
        assertEquals(equity, newManager.getEquity());
        assertEquals(initialBalance, newManager.getInitialBalance());
    }

    @Test
    void testNewConstructor() {
        Number initialBalance = new Number(2000.0);
        DefaultAccountManager newManager = new DefaultAccountManager(initialBalance);

        assertEquals(initialBalance, newManager.getBalance());
        assertEquals(initialBalance, newManager.getEquity());
        assertEquals(initialBalance, newManager.getInitialBalance());
    }

}