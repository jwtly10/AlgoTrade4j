package dev.jwtly10.shared.config.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, String role, int limit, int duration) {
        return cache.computeIfAbsent(key, k -> newBucket(role, limit, duration));
    }

    private Bucket newBucket(String role, int limit, int duration) {
        Bandwidth bandwidth = getBandwidthForRole(role, limit, duration);
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private Bandwidth getBandwidthForRole(String role, int limit, int duration) {
        if ("ADMIN".equals(role)) {
            return Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))); // Admins get 100 requests per minute by default
        } else {
            return Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofSeconds(duration)));
        }
    }
}