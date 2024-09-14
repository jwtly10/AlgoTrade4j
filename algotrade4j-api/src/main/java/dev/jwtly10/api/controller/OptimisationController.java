package dev.jwtly10.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.jwtly10.api.exception.StrategyManagerException;
import dev.jwtly10.api.model.StrategyConfig;
import dev.jwtly10.api.model.optimisation.OptimisationResultDTO;
import dev.jwtly10.api.model.optimisation.OptimisationTask;
import dev.jwtly10.api.service.optimisation.OptimisationService;
import dev.jwtly10.shared.auth.utils.SecurityUtils;
import dev.jwtly10.shared.config.ratelimit.RateLimit;
import dev.jwtly10.shared.exception.ErrorType;
import dev.jwtly10.shared.tracking.TrackingService;
import dev.jwtly10.shared.tracking.UserAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/optimisation")
@Slf4j
public class OptimisationController {
    private final OptimisationService optimisationService;
    private final TrackingService trackingService;

    public OptimisationController(OptimisationService optimisationService, TrackingService trackingService) {
        this.optimisationService = optimisationService;
        this.trackingService = trackingService;
    }

    @PostMapping("/queue")
    @RateLimit(limit = 5)
    public ResponseEntity<OptimisationTask> queueOptimisation(@RequestBody StrategyConfig config) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("Queueing optimisation for user: {} with config: {}", userId, config);
        trackingService.track(userId, UserAction.OPTIMISATION_RUN, Map.of("optimisation_config", config));

        try {
            OptimisationTask task = optimisationService.queueOptimisation(config, userId);
            return ResponseEntity.accepted().body(task);
        } catch (IllegalArgumentException e) {
            throw new StrategyManagerException(e.getMessage(), ErrorType.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            // This will be an error with our internal objects & JSON serialisation
            throw new StrategyManagerException(e.getMessage(), ErrorType.INTERNAL_ERROR);
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<OptimisationTask>> getUserTasks() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<OptimisationTask> tasks = optimisationService.getUserTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable("taskId") Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.debug("Deleting task: {} for user: {}", taskId, currentUserId);

        trackingService.track(currentUserId, UserAction.OPTIMISATION_DELETE, Map.of("taskId", taskId));

        optimisationService.deleteTask(taskId, currentUserId);
        return ResponseEntity.ok("Task deleted successfully");
    }

    @GetMapping("/tasks/{taskId}/results")
    @RateLimit(limit = 15)
    public ResponseEntity<List<OptimisationResultDTO>> getResultsForTask(@PathVariable("taskId") Long taskId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.debug("Getting results for task: {} for user: {}", taskId, currentUserId);

        trackingService.track(currentUserId, UserAction.OPTIMISATION_RESULTS, Map.of("taskId", taskId));

        try {
            List<OptimisationResultDTO> results = optimisationService.getResultsForTask(taskId, currentUserId);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            throw new StrategyManagerException(e.getMessage(), ErrorType.BAD_REQUEST);
        } catch (Exception e) {
            throw new StrategyManagerException(e.getMessage(), ErrorType.INTERNAL_ERROR);
        }
    }

    @PostMapping("/share/{taskId}/{shareWithUserId}")
    public ResponseEntity<String> shareTask(@PathVariable("taskId") Long taskId, @PathVariable("shareWithUserId") Long shareWithUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.debug("Sharing task: {} from user: {} with user: {}", taskId, currentUserId, shareWithUserId);

        try {
            optimisationService.shareTask(taskId, currentUserId, shareWithUserId);
            return ResponseEntity.ok("Shared Successfully");
        } catch (IllegalArgumentException e) {
            throw new StrategyManagerException(e.getMessage(), ErrorType.BAD_REQUEST);
        } catch (Exception e) {
            throw new StrategyManagerException(e.getMessage(), ErrorType.INTERNAL_ERROR);
        }
    }
}