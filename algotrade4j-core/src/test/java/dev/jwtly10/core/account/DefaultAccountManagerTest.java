package dev.jwtly10.core.account;

import dev.jwtly10.core.model.Number;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultAccountManagerTest {

    private DefaultAccountManager accountManager;
    private Number initialBalance;
    private Number initialEquity;

    @BeforeEach
    void setUp() {
        initialBalance = new Number(1000.0);
        initialEquity = new Number(500.0);
        Number initialInitialBalance = new Number(1000.0);
        accountManager = new DefaultAccountManager(initialBalance, initialEquity, initialInitialBalance);
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
        assertEquals(initialEquity, accountManager.getEquity());
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
    void testConstructor() {
        Number balance = new Number(2000.0);
        Number equity = new Number(1000.0);
        Number initialBalance = new Number(2000.0);
        DefaultAccountManager newManager = new DefaultAccountManager(balance, equity, initialBalance);

        assertEquals(balance, newManager.getBalance());
        assertEquals(equity, newManager.getEquity());
        assertEquals(initialBalance, newManager.getInitialBalance());
    }
}