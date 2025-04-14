package com.turkcell.billingservice.aspects;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

@Aspect
@Component
public class RateLimiterAspect {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Around("@annotation(rateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String key = getClientIp();
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(rateLimited.value()));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return joinPoint.proceed();
        } else {
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            response.setStatus(429);
            response.getWriter().write("Too many requests");
            return null;
        }
    }

    private Bucket createBucket(int rateLimit) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(rateLimit, Refill.greedy(rateLimit, Duration.ofMinutes(1))))
                .build();
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getRemoteAddr();
        }
        return "unknown";
    }
} 