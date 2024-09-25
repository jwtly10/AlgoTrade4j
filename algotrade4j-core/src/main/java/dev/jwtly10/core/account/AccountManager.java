package dev.jwtly10.core.account;

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
    double getBalance();

    /**
     * Sets the balance of the account.
     *
     * @param balance the new balance to set
     */
    void setBalance(double balance);

    /**
     * Gets the current equity of the account.
     *
     * @return the current equity
     */
    double getEquity();

    /**
     * Sets the equity of the account.
     *
     * @param equity the new equity to set
     */
    void setEquity(double equity);

    /**
     * Gets the initial balance of the account.
     *
     * @return the initial balance
     */
    double getInitialBalance();

    /**
     * Gets the total value of all open positions.
     *
     * @return the total value of open positions
     */
    double getOpenPositionValue();

    /**
     * Get the account instance
     *
     * @return the account instance
     */
    Account getAccount();

    /**
     * Update the account information.
     *
     * @param account the new account information
     */
    void updateAccountInfo(Account account);
}