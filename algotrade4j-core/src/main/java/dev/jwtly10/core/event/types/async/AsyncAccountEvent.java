package dev.jwtly10.core.event.types.async;

import dev.jwtly10.core.account.Account;
import dev.jwtly10.core.event.BaseEvent;
import lombok.Getter;

/**
 * Represents an async account event in the trading system.
 */
@Getter
public class AsyncAccountEvent extends BaseEvent {
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
    public AsyncAccountEvent(String strategyId, Account account) {
        super(strategyId, "ASYNC_ACCOUNT", null);
        this.account = account;
    }
}