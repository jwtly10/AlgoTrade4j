package dev.jwtly10.api.controller;

import dev.jwtly10.shared.tracking.TrackingService;
import dev.jwtly10.shared.tracking.UserActionLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tracking")
public class UserTrackingController {

    private final TrackingService trackingService;

    public UserTrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<UserActionLog>> getTrackingEventsForUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(trackingService.getTrackingEventsForUser(userId));
    }
}