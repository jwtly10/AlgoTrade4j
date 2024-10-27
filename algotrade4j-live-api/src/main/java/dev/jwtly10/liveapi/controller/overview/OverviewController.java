package dev.jwtly10.liveapi.controller.overview;

import dev.jwtly10.liveapi.model.activity.ActivitySnapshot;
import dev.jwtly10.liveapi.model.strategy.LiveStrategy;
import dev.jwtly10.liveapi.service.activity.ActivityService;
import dev.jwtly10.liveapi.service.strategy.LiveStrategyService;
import dev.jwtly10.shared.exception.ApiException;
import dev.jwtly10.shared.exception.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for overview of the live api
 * Used for populating algortade4j.trade/ homepage
 */
@RestController
@RequestMapping("api/v1/overview")
@Slf4j
public class OverviewController {

    private final LiveStrategyService liveStrategyService;
    private final ActivityService activityService;


    public OverviewController(LiveStrategyService liveStrategyService, ActivityService activityService) {
        this.liveStrategyService = liveStrategyService;
        this.activityService = activityService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getOverviewStats() {
        // Number of live strategies

        int totalLive = 0;
        int totalActive = 0;
        int totalPaused = 0;
        var res = liveStrategyService.getNonHiddenLiveStrategies();
        totalLive = res.size();
        totalActive = res.stream().filter(LiveStrategy::isActive).toList().size();
        totalPaused = totalLive - totalActive;


        return null;
    }

    @GetMapping("/activities")
    public ResponseEntity<List<ActivitySnapshot>> getRecentActivity() {
        try {
            var res = activityService.getRecentActivity(15);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error getting recent activity data:", e);
            throw new ApiException("Error getting recent activity", ErrorType.INTERNAL_ERROR);
        }
    }
}