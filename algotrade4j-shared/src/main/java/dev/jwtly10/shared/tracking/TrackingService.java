package dev.jwtly10.shared.tracking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A small utility service to track user actions and save them to the database, for reporting and analysis.
 * Also for security - ensuring users are not abusing system
 * Uses an async queue for performance
 */
@Service
@Slf4j
public class TrackingService {
    private final ConcurrentLinkedQueue<UserActionLog> eventQueue = new ConcurrentLinkedQueue<>();
    private final UserActionLogRepository userActionLogRepository;

    public TrackingService(UserActionLogRepository userActionLogRepository) {
        this.userActionLogRepository = userActionLogRepository;
    }

    @Async
    public void track(Long userId, UserAction action, Map<String, Object> metaData) {
        UserActionLog log = new UserActionLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setMetaData(metaData);
        log.setTimestamp(ZonedDateTime.now());

        eventQueue.offer(log);
    }

    @Scheduled(fixedRate = 60000)
    public void flushLogs() {
        List<UserActionLog> logs = new ArrayList<>();
        UserActionLog userLog;
        while ((userLog = eventQueue.poll()) != null) {
            logs.add(userLog);
        }

        if (!logs.isEmpty()) {
            try {
                userActionLogRepository.saveAll(logs);
            } catch (Exception e) {
                log.error("Failed to save user action logs: {}", e.getMessage(), e);
            }
        }
    }

    public List<UserActionLog> getTrackingEventsForUser(Long userId) {
        return userActionLogRepository.findByUserId(userId);
    }

    public List<UserActionLog> getAllTrackingEvents() {
        return userActionLogRepository.findAll();
    }

    public List<UserActionLog> getRecentTrackingEvents(int limit) {
        return userActionLogRepository.findAllByOrderByIdDesc(PageRequest.of(0, limit));
    }
}