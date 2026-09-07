package com.example.zongshe1.modules.risk.datasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 并发拉取所有已启用的外部数据源。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExternalDataOrchestrator {

    private final List<ExternalDataSourceAdapter> adapters;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public List<ExternalDataFetchResult> fetchAll(DataSourceQuery query) {
        List<ExternalDataSourceAdapter> enabled = adapters.stream()
                .filter(ExternalDataSourceAdapter::isEnabled)
                .filter(a -> a.getSourceType() != DataSourceType.INTERNAL_DB)
                .collect(Collectors.toList());

        if (enabled.isEmpty()) {
            return List.of();
        }

        List<CompletableFuture<ExternalDataFetchResult>> futures = new ArrayList<>();
        for (ExternalDataSourceAdapter adapter : enabled) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> safeFetch(adapter, query), executor)
                    .orTimeout(adapter.getTimeoutMs(), TimeUnit.MILLISECONDS)
                    .exceptionally(ex -> {
                        log.warn("数据源拉取超时或异常：type={}, msg={}", adapter.getSourceType(), ex.getMessage());
                        return ExternalDataFetchResult.failed(adapter.getSourceType(), ex.getMessage());
                    }));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private static ExternalDataFetchResult safeFetch(ExternalDataSourceAdapter adapter, DataSourceQuery query) {
        try {
            return adapter.fetch(query);
        } catch (Exception e) {
            log.warn("数据源拉取失败：type={}, error={}", adapter.getSourceType(), e.getMessage());
            return ExternalDataFetchResult.failed(adapter.getSourceType(), e.getMessage());
        }
    }
}
