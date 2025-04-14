package com.turkcell.billingservice.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * API isteklerini sınırlamak için interceptor.
 * HTTP istekleri geldiğinde devreye girer ve rate limiting uygular.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Autowired
    @Qualifier("defaultBucket")
    private final Bucket defaultBucket;
    
    @Autowired
    @Qualifier("billApiBucket")
    private final Bucket billApiBucket;
    
    @Autowired
    @Qualifier("paymentApiBucket")
    private final Bucket paymentApiBucket;
    
    @Autowired
    @Qualifier("reportApiBucket")
    private final Bucket reportApiBucket;
    
    private final RateLimitingConfig rateLimitingConfig;

    /**
     * İstek işlenmeden önce rate limiting kontrolü yapar.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Bucket targetBucket = selectBucket(request);
        String clientId = getClientIdentifier(request);
        
        if (clientId != null) {
            targetBucket = rateLimitingConfig.resolveBucket(clientId);
        }
        
        ConsumptionProbe probe = targetBucket.tryConsumeAndReturnRemaining(1);
        
        if (!probe.isConsumed()) {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Try again in %d seconds\"}", 
                waitForRefill
            ));
            
            log.warn("Rate limit exceeded for client: {}, path: {}", clientId, request.getRequestURI());
            return false;
        }
        
        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
        return true;
    }
    
    /**
     * İstek yoluna göre uygun bucket'ı seçer.
     */
    private Bucket selectBucket(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        if (path.startsWith("/api/v1/bills")) {
            return billApiBucket;
        } else if (path.startsWith("/api/v1/payments")) {
            return paymentApiBucket;
        } else if (path.startsWith("/api/v1/reports")) {
            return reportApiBucket;
        }
        
        return defaultBucket;
    }
    
    /**
     * İstek yapan kullanıcı veya client için benzersiz tanımlayıcı döndürür.
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // Öncelikle authentication kullanıcı adını kontrol et
        String username = request.getUserPrincipal() != null ? 
                request.getUserPrincipal().getName() : null;
        
        if (username != null) {
            return "user:" + username;
        }
        
        // API Key veya Token kontrol et
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) {
            return "apikey:" + apiKey;
        }
        
        // Son çare olarak IP adresi kullan
        return "ip:" + request.getRemoteAddr();
    }
} 