package dev.jwtly10.api.config.ratelimit;

import dev.jwtly10.api.auth.model.UserDetailsImpl;
import dev.jwtly10.common.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private final RateLimitConfig rateLimitConfig;

    public RateLimitAspect(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Around("execution(* dev.jwtly10.api..*Controller.*(..))")
    public Object limitRate(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        int limit = 30; // default limit
        int duration = 60; // default duration

        if (rateLimit != null) {
            limit = rateLimit.limit();
            duration = rateLimit.duration();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String key;

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            key = userDetails.getId() + ":" + joinPoint.getSignature().toShortString();
        } else {
            // Handle unauthenticated or anonymous users
            key = "anonymous:" + joinPoint.getSignature().toShortString();
        }

        io.github.bucket4j.Bucket bucket = rateLimitConfig.resolveBucket(key, "ROLE_ANONYMOUS", limit, duration);

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            String userIdentifier;
            String userRole;

            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                userIdentifier = userDetails.getUsername(); // or userDetails.getId(), depending on what you prefer
                userRole = userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(", "));
            } else {
                userIdentifier = "anonymous";
                userRole = "ROLE_ANONYMOUS";
            }

            String methodName = joinPoint.getSignature().toShortString();

            log.warn("Rate limit exceeded for user '{}' with role(s) '{}' on endpoint '{}'. Limit: {}, Duration: {} seconds",
                    userIdentifier, userRole, methodName, limit, duration);

            throw new RateLimitException("Rate limit exceeded for this endpoint");
        }
    }
}