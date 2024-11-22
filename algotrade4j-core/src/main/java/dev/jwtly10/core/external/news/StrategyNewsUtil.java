package dev.jwtly10.core.external.news;

import dev.jwtly10.core.external.news.forexfactory.ForexFactoryClient;
import dev.jwtly10.core.external.news.forexfactory.ForexFactoryNews;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.List;

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
     * THIS CONSTRUCTOR IS ONLY USED FOR TESTING - DO NOT USE IN PRODUCTION
     * This constructor is used to inject a mock Forex Factory for testing purposes
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
     * @return the news event that is the next high impact event for the given country, or null if none (or backtesting)
     * @throws IOException if there is an error fetching the news data
     */
    @Nullable
    public ForexFactoryNews getNextHighImpactEvent(String country, ZonedDateTime currentDt) throws IOException {
        // During backtesting we do not have news data so this logic does not work
        // To prevent unexpected logic during a backtest, we will return null so the user can't do any action on news
        if (!isLive) {
            return null;
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

        return nextHighImpactEvent;
    }

    /**
     * Returns the last high impact event for the given country
     *
     * @param country   the currency country code to get the next high impact event for
     * @param currentDt the current date and time
     * @return the news event that is the last high impact event for the given country, or null if none (or backtesting)
     * @throws IOException if there is an error fetching the news data
     */
    @Nullable
    public ForexFactoryNews getLastHighImpactEvent(String country, ZonedDateTime currentDt) throws IOException {
        // During backtesting we do not have news data so this logic does not work
        // To prevent unexpected logic during a backtest, we will return null so the user can't do any action on news
        if (!isLive) {
            return null;
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

        return lastHighImpactEvent;

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