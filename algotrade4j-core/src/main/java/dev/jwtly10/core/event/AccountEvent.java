package dev.jwtly10.core.event;

import dev.jwtly10.core.account.Account;
import lombok.Getter;

/**
 * Represents an account event in the trading system.
 */
@Getter
public class AccountEvent extends BaseEvent {
    /**
     * The account associated with the event.
     */
    private final Account account;

    /**
     * Constructs an AccountEvent with the specified parameters.
     *
     * @param strategyId the unique identifier of the strategy
     * @param account    the account associated with the event
     */
    public AccountEvent(String strategyId, Account account) {
        super(strategyId, "ACCOUNT", "");
        this.account = account;
    }
}