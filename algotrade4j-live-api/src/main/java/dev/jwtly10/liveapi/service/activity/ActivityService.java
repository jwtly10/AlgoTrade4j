package dev.jwtly10.liveapi.service.activity;

import dev.jwtly10.liveapi.model.activity.ActivitySnapshot;
import dev.jwtly10.shared.tracking.TrackingService;
import dev.jwtly10.shared.tracking.UserActionLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Activity service is a wrapper around all the logging and activity tracking we have in the system.
 * It provides a consolidated overview of all the activities happening in the system.
 */
@Service
public class ActivityService {

    private final TrackingService trackingService;

    public ActivityService(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    public List<ActivitySnapshot> getRecentActivity(int limit) {
        // Extending the initial limit by 100 to account for any logs that might be not wanted (LOGINS, LOGOUTS, etc)
        List<UserActionLog> recentLogs = trackingService.getRecentTrackingEvents(limit + 100);
        return recentLogs.stream()
                .map(this::formatActivityLog)
                .filter(activity -> activity.getDescription() != null)
//                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private ActivitySnapshot formatActivityLog(UserActionLog log) {
        String description = switch (log.getAction()) {
            case LIVE_STRATEGY_CREATE -> formatLiveStrategyCreate(log);
            case LIVE_STRATEGY_EDIT -> formatLiveStrategyEdit(log);
            case LIVE_STRATEGY_TOGGLE -> formatLiveStrategyToggle(log);
            case BACKTEST_RUN -> formatBacktestRun(log);
            case OPTIMISATION_RUN -> formatOptimisationRun(log);
            default -> null; // We don't care about other logs for now
        };

        return new ActivitySnapshot(description, log.getTimestamp());
    }

    private String formatLiveStrategyCreate(UserActionLog log) {
        String strategyName = getStringFromMetadata(log, "strategyName");
        if (strategyName == null) {
            return "New live strategy created";
        }
        return String.format("New live strategy '%s' created", strategyName);
    }

    private String formatLiveStrategyEdit(UserActionLog log) {
        String strategyName = getStringFromMetadata(log, "strategyName");
        if (strategyName == null) {
            return "Live Strategy configuration updated";
        }
        return String.format("Live Strategy '%s' edited", strategyName);
    }

    private String formatLiveStrategyToggle(UserActionLog log) {
        String strategyName = getStringFromMetadata(log, "strategyName");
        Boolean toggledOn = getBooleanFromMetadata(log, "toggledOn");

        if (strategyName == null) {
            return toggledOn != null ?
                    (toggledOn ? "Live Strategy activated" : "Live Strategy deactivated") :
                    "Live Strategy status changed";
        }

        return String.format("Live Strategy '%s' %s", strategyName,
                toggledOn != null ? (toggledOn ? "activated" : "deactivated") : "status changed");
    }

    private String formatBacktestRun(UserActionLog log) {
        String strategyClass = getStringFromMetadata(log, "strategyClass");
        if (strategyClass == null) {
            return "Backtest completed";
        }
        return String.format("Backtest completed for '%s'", strategyClass);
    }

    private String formatOptimisationRun(UserActionLog log) {
        String strategyClass = getStringFromMetadata(log, "strategyClass");
        if (strategyClass == null) {
            return "New optimization run started";
        }
        return String.format("New optimization run started for '%s'", strategyClass);
    }

    // Helper methods to safely extract values from metadata
    private String getStringFromMetadata(UserActionLog log, String key) {
        if (log.getMetaData() == null || !log.getMetaData().containsKey(key)) {
            return null;
        }
        Object value = log.getMetaData().get(key);
        return value != null ? value.toString() : null;
    }

    private Boolean getBooleanFromMetadata(UserActionLog log, String key) {
        if (log.getMetaData() == null || !log.getMetaData().containsKey(key)) {
            return null;
        }
        Object value = log.getMetaData().get(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }
}