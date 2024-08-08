package dev.jwtly10.core.account;

import dev.jwtly10.core.model.Number;

/**
 * Interface for managing account-related operations.
 * Each broker should have their own implementation of this interface.
 */
public interface AccountManager {

    /**
     * Gets the current balance of the account.
     *
     * @return the current balance
     */
    Number getBalance();

    /**
     * Sets the balance of the account.
     *
     * @param balance the new balance to set
     */
    void setBalance(Number balance);

    /**
     * Gets the current equity of the account.
     *
     * @return the current equity
     */
    Number getEquity();

    /**
     * Sets the equity of the account.
     *
     * @param equity the new equity to set
     */
    void setEquity(Number equity);

    /**
     * Gets the initial balance of the account.
     *
     * @return the initial balance
     */
    Number getInitialBalance();
}