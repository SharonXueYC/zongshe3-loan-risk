package com.example.zongshe1.modules.risk.datasource;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 * 单个数据源一次拉取的结果。
 */
public record ExternalDataFetchResult(
        DataSourceType sourceType,
        boolean success,
        String message,
        Map<String, Object> features,
        Instant fetchedAt
) {
    public static ExternalDataFetchResult ok(DataSourceType type, Map<String, Object> features) {
        return new ExternalDataFetchResult(
                type,
                true,
                "OK",
                features == null ? Map.of() : Map.copyOf(features),
                Instant.now()
        );
    }

    public static ExternalDataFetchResult failed(DataSourceType type, String message) {
        return new ExternalDataFetchResult(
                type,
                false,
                message,
                Collections.emptyMap(),
                Instant.now()
        );
    }
}
