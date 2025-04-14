package com.turkcell.billingservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API isteklerini sınırlamak için rate limiting konfigürasyonu.
 */
@Configuration
public class RateLimitingConfig {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Rate limiting bucket'larını tutan haritayı döndürür.
     *
     * @return Bucket haritası
     */
    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }

    /**
     * Belirtilen anahtar için bir bucket oluşturur veya mevcut bucket'ı döndürür.
     * Her kullanıcıya veya IP adresine özel bucket oluşturmak için kullanılabilir.
     *
     * @param key Bucket anahtarı (örn. kullanıcı adı veya IP adresi)
     * @return İstek limiti için bucket
     */
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> {
            Refill refill = Refill.greedy(100, Duration.ofMinutes(1));
            Bandwidth limit = Bandwidth.classic(100, refill);
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * API endpointleri için varsayılan bucket'ı döndürür.
     * 
     * @return Varsayılan bucket
     */
    @Bean(name = "defaultBucket")
    public Bucket defaultBucket() {
        Refill refill = Refill.greedy(100, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(100, refill);
        return Bucket.builder().addLimit(limit).build();
    }
    
    /**
     * Fatura API'si için bucket'ı döndürür.
     * 
     * @return Fatura API bucket'ı
     */
    @Bean(name = "billApiBucket")
    public Bucket billApiBucket() {
        Refill refill = Refill.greedy(50, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(50, refill);
        return Bucket.builder().addLimit(limit).build();
    }
    
    /**
     * Ödeme API'si için bucket'ı döndürür.
     * 
     * @return Ödeme API bucket'ı
     */
    @Bean(name = "paymentApiBucket")
    public Bucket paymentApiBucket() {
        Refill refill = Refill.greedy(30, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(30, refill);
        return Bucket.builder().addLimit(limit).build();
    }
    
    /**
     * Rapor API'si için bucket'ı döndürür.
     * 
     * @return Rapor API bucket'ı
     */
    @Bean(name = "reportApiBucket")
    public Bucket reportApiBucket() {
        Refill refill = Refill.greedy(10, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(10, refill);
        return Bucket.builder().addLimit(limit).build();
    }
} 