package com.example.zongshe1.modules.ops.cache;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ProductCacheMetrics {
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong invalidations = new AtomicLong();

    public void hit() { hits.incrementAndGet(); }
    public void miss() { misses.incrementAndGet(); }
    public void invalidate() { invalidations.incrementAndGet(); }

    public long hits() { return hits.get(); }
    public long misses() { return misses.get(); }
    public long invalidations() { return invalidations.get(); }
}