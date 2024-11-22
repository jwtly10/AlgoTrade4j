package dev.jwtly10.core.external.news;

import dev.jwtly10.core.external.news.forexfactory.ForexFactoryClient;
import dev.jwtly10.core.external.news.forexfactory.ForexFactoryNews;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * StrategyNewsUtil is a layer of abstraction over the news data we collect each day
 * This gives strategies easy access to new data to make informed decisions in their trading strategies
 */
@Slf4j
public class StrategyNewsUtil {
    private final ForexFactoryClient forexFactoryClient;
    private boolean useMockedData = false;
    private boolean isLive = true;

    public StrategyNewsUtil(ForexFactoryClient forexFactoryClient, boolean isLive) {
        this.forexFactoryClient = forexFactoryClient;
        this.isLive = isLive;
    }

    /**
     * THIS CONSTRUCTOR IS ONLY USED FOR TESTING OR BACKTESTING
     * This constructor is used to create a StrategyNewsUtil object that will use mocked data, and WILL NOT BE TRIGGERED.
     * Backtesting does not have access to news data, so we can't make trading decisions based on this.
     */

    public StrategyNewsUtil() {
        this.forexFactoryClient = new ForexFactoryClient();
        this.useMockedData = true;
    }

    /**
     * Returns the next high impact event for the given country
     *
     * @param country   the currency country code to get the next high impact event for
     * @param currentDt the current date and time
     * @return optional of the news event that is the next high impact event for the given country, or empty if none (or backtesting)
     * @throws IOException if there is an error fetching the news data
     */
    public Optional<ForexFactoryNews> getNextHighImpactEvent(String country, ZonedDateTime currentDt) throws IOException {
        // During backtesting we do not have news data so this logic does not work
        // To prevent unexpected logic during a backtest, we will return null so the user can't do any action on news
        if (!isLive) {
            return Optional.empty();
        }

        List<ForexFactoryNews> allNews = getNews();

        ForexFactoryNews nextHighImpactEvent = null;
        for (ForexFactoryNews news : allNews) {
            if (news.country().equalsIgnoreCase(country) && news.impact().getValue().equalsIgnoreCase("High") && news.date().isAfter(currentDt)) {
                if (nextHighImpactEvent == null || news.date().isBefore(nextHighImpactEvent.date())) {
                    nextHighImpactEvent = news;
                }
            }
        }

        if (nextHighImpactEvent == null) {
            return Optional.empty();
        }

        return Optional.of(nextHighImpactEvent);
    }

    /**
     * Returns the last high impact event for the given country
     *
     * @param country   the currency country code to get the next high impact event for
     * @param currentDt the current date and time
     * @return optional of the news event that is the last high impact event for the given country, or empty if none (or backtesting)
     * @throws IOException if there is an error fetching the news data
     */
    public Optional<ForexFactoryNews> getLastHighImpactEvent(String country, ZonedDateTime currentDt) throws IOException {
        // During backtesting we do not have news data so this logic does not work
        // To prevent unexpected logic during a backtest, we will return null so the user can't do any action on news
        if (!isLive) {
            return Optional.empty();
        }

        List<ForexFactoryNews> allNews = getNews();

        ForexFactoryNews lastHighImpactEvent = null;
        for (ForexFactoryNews news : allNews) {
            if (news.country().equalsIgnoreCase(country) && news.impact().getValue().equalsIgnoreCase("High") && news.date().isBefore(currentDt)) {
                if (lastHighImpactEvent == null || news.date().isAfter(lastHighImpactEvent.date())) {
                    lastHighImpactEvent = news;
                }
            }
        }

        if (lastHighImpactEvent == null) {
            return Optional.empty();
        }

        return Optional.of(lastHighImpactEvent);
    }

    private List<ForexFactoryNews> getNews() throws IOException {
        if (useMockedData) {
            try {
                return forexFactoryClient.getMockedNews();
            } catch (IOException | URISyntaxException e) {
                log.warn("This exception should have only happened during unit tests. If not this NEEDS to be reviewed.", e);
                return List.of();
            }
        } else {
            return forexFactoryClient.getThisWeeksNews();
        }
    }
}