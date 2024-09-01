package dev.jwtly10.api.config.ratelimit;

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
        if ("ROLE_ADMIN".equals(role)) {
            return Bandwidth.classic(Integer.MAX_VALUE, Refill.intervally(Integer.MAX_VALUE, Duration.ofSeconds(1)));
        } else {
            return Bandwidth.classic(limit, Refill.intervally(limit, Duration.ofSeconds(duration)));
        }
    }
}