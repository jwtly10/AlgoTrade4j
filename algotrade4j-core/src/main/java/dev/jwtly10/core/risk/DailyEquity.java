package dev.jwtly10.core.risk;

import java.time.ZonedDateTime;


/**
 * Represents the daily equity of an account
 * @param accountId the account id
 * @param lastEquity the last equity of the account
 * @param updatedAt the time the equity was last updated IN UTC!
 */
public record DailyEquity(String accountId, Double lastEquity, ZonedDateTime updatedAt) {
}
