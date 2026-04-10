package br.com.everton.backendextrato.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                buildCache(CacheNames.ACCOUNT_CATEGORIES, 20, 200, Duration.ofMinutes(30)),
                buildCache(CacheNames.DEBT_CATEGORIES, 20, 200, Duration.ofMinutes(30)),
                buildCache(CacheNames.ACCOUNT_BILLS, 20, 300, Duration.ofMinutes(10)),
                buildCache(CacheNames.ACCOUNT_BILL_BY_ID, 20, 500, Duration.ofMinutes(10)),
                buildCache(CacheNames.DEBTS, 20, 300, Duration.ofMinutes(10)),
                buildCache(CacheNames.INCOME_ENTRIES, 20, 300, Duration.ofMinutes(10)),
                buildCache(CacheNames.INCOME_ENTRY_BY_ID, 20, 500, Duration.ofMinutes(10)),
                buildCache(CacheNames.COMPANY_PROFILE, 10, 150, Duration.ofMinutes(20)),
                buildCache(CacheNames.FINANCIAL_SEPARATION_WORKSPACE, 10, 150, Duration.ofMinutes(5)),
                buildCache(CacheNames.ACCESS_MANAGEMENT, 10, 150, Duration.ofMinutes(5))
        ));
        return cacheManager;
    }

    private Cache buildCache(String cacheName, int initialCapacity, long maximumSize, Duration ttl) {
        return new CaffeineCache(cacheName, Caffeine.newBuilder()
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .expireAfterWrite(ttl)
                .recordStats()
                .build());
    }
}
