package com.example.zongshe1.modules.risk.feature;

import com.example.zongshe1.modules.risk.datasource.DataSourceType;
import com.example.zongshe1.modules.risk.datasource.ExternalDataFetchResult;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * 一次风控评估使用的变量快照（库内 + 外部源合并）。
 */
public class FeatureSnapshot {

    private final Map<String, Object> features = new HashMap<>();
    private final Map<DataSourceType, ExternalDataFetchResult> sourceResults = new EnumMap<>(DataSourceType.class);
    private Instant builtAt = Instant.now();

    public void put(String key, Object value) {
        features.put(key, value);
    }

    public void putAll(Map<String, Object> map) {
        if (map != null) {
            features.putAll(map);
        }
    }

    public void recordSource(ExternalDataFetchResult result) {
        sourceResults.put(result.sourceType(), result);
        if (result.success()) {
            putAll(result.features());
        }
    }

    public Object get(String key) {
        return features.get(key);
    }

    public Integer getInt(String key, int defaultValue) {
        Object v = features.get(key);
        if (v instanceof Number n) {
            return n.intValue();
        }
        return defaultValue;
    }

    public Map<String, Object> getFeatures() {
        return Collections.unmodifiableMap(features);
    }

    public Map<DataSourceType, ExternalDataFetchResult> getSourceResults() {
        return Collections.unmodifiableMap(sourceResults);
    }

    public Instant getBuiltAt() {
        return builtAt;
    }

    public void setBuiltAt(Instant builtAt) {
        this.builtAt = builtAt;
    }
}
