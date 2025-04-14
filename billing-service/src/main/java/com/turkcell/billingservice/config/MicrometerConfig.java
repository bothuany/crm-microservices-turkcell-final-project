package com.turkcell.billingservice.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer metrik konfigürasyonu.
 * Bu konfigürasyon, uygulama tarafından üretilen metriklerin Prometheus'a aktarılmasını sağlar.
 */
@Configuration
public class MicrometerConfig {

    /**
     * JVM ve sistem metriklerini kayıt eder.
     *
     * @param registry Metrik kaydedicisi
     */
    @Bean
    public void registerMetrics(MeterRegistry registry) {
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
    }

    /**
     * Metotların çalışma sayısını ölçen aspect.
     *
     * @param registry Metrik kaydedicisi
     * @return CountedAspect
     */
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    /**
     * Metotların çalışma süresini ölçen aspect.
     *
     * @param registry Metrik kaydedicisi
     * @return TimedAspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
} 