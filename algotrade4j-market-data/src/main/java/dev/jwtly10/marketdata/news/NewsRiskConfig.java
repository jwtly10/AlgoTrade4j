package dev.jwtly10.marketdata.news;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * The NewsRiskConfig class represents the configuration for the news risk management.
 * It enables users to set configuration for whether trades must be closed before a certain news event or all events
 */
@Data
public class NewsRiskConfig {
    // Whether news risk management is enabled
    private boolean enabled;
    // The country this risk configuration is for
    private String country;
    // Whether to close all trades before news events
    private int closeXMinBeforeNews;
    // How long after the triggered news event to start opening trades
    private int openXMinAfterNews;

    // Pattern matched news events to close trades on
    private Set<String> eventPatterns = new HashSet<>();

    // Specific impact levels to close trades on
    private Set<NewsImpact> impactLevels = new HashSet<>();

    private boolean closeOnAllNews = false;

    // Builder pattern for flexible construction
    public static class Builder {
        private final NewsRiskConfig config = new NewsRiskConfig();

        public Builder enabled(boolean enabled) {
            config.enabled = enabled;
            return this;
        }

        public Builder closeXMinBeforeNews(int minutes) {
            config.closeXMinBeforeNews = minutes;
            return this;
        }

        public Builder openXMinAfterNews(int minutes) {
            config.openXMinAfterNews = minutes;
            return this;
        }

        public Builder addEventPattern(String pattern) {
            config.eventPatterns.add(pattern);
            return this;
        }

        public Builder addImpactLevel(NewsImpact impact) {
            config.impactLevels.add(impact);
            return this;
        }

        public Builder closeOnAllNews(boolean value) {
            config.closeOnAllNews = value;
            return this;
        }

        public NewsRiskConfig build() {
            return config;
        }
    }

    /**
     * Given a news event. Does the configuration require closing trades on this event?
     * @param event The news event
     * @return Whether trades should be closed on this event
     */
    public boolean shouldCloseOnNews(GenericNewsEvent event) {
        if (!enabled) return false;

        // Check if we're closing on all events of specific impact
        if (closeOnAllImpactEvents && impactLevels.contains(event.getImpact())) {
            return true;
        }

        // Check specific event patterns
        return eventPatterns.stream()
                .anyMatch(pattern -> matchesPattern(event.getTitle(), pattern));
    }

    private boolean matchesPattern(String title, String pattern) {
        // Convert the pattern to regex (replace * with .*)
        String regex = pattern.replace("*", ".*");
        return title.matches(regex);
    }

}