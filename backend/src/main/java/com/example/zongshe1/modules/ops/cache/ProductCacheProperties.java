package com.example.zongshe1.modules.ops.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ops.cache.products")
public class ProductCacheProperties {
    private boolean enabled = true;
    private long ttlSeconds = 90;
    private long maxSize = 256;
}