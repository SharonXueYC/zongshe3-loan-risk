package com.example.zongshe1.modules.ops.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ProductCacheConfig {

    @Bean
    public Cache<String, byte[]> productListCache(ProductCacheProperties props) {
        return Caffeine.newBuilder()
                .maximumSize(props.getMaxSize())
                .expireAfterWrite(Duration.ofSeconds(props.getTtlSeconds()))
                .recordStats()
                .build();
    }
}