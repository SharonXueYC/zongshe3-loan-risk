package com.example.zongshe1.modules.risk.datasource.impl;

import com.example.zongshe1.modules.crawler.service.ExternalDataCacheService;
import com.example.zongshe1.modules.risk.datasource.DataSourceQuery;
import com.example.zongshe1.modules.risk.datasource.DataSourceType;
import com.example.zongshe1.modules.risk.datasource.ExternalDataFetchResult;
import com.example.zongshe1.modules.risk.datasource.ExternalDataSourceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 征信适配器：优先读取爬取缓存，无缓存时使用本地规则兜底。
 */
@Component
@RequiredArgsConstructor
public class MockCreditBureauAdapter implements ExternalDataSourceAdapter {

    private final ExternalDataCacheService cacheService;

    @Value("${datasource.credit-bureau.enabled:true}")
    private boolean enabled;

    @Value("${datasource.credit-bureau.timeout-ms:3000}")
    private long timeoutMs;

    @Value("${datasource.credit-bureau.required:false}")
    private boolean required;

    @Override
    public DataSourceType getSourceType() {
        return DataSourceType.CREDIT_BUREAU;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public long getTimeoutMs() {
        return timeoutMs;
    }

    @Override
    public ExternalDataFetchResult fetch(DataSourceQuery query) {
        String suffix = extractSuffix(query.idCardNumber());
        var cached = cacheService.getPayload(
                DataSourceType.CREDIT_BUREAU.name(), "SUFFIX_" + suffix, "");
        if (cached.isPresent()) {
            return ExternalDataFetchResult.ok(getSourceType(), cached.get());
        }
        return ExternalDataFetchResult.ok(getSourceType(), fallbackPayload(query));
    }

    private static String extractSuffix(String idCard) {
        if (idCard == null || idCard.isBlank()) {
            return "0";
        }
        char last = idCard.charAt(idCard.length() - 1);
        return Character.isDigit(last) ? String.valueOf(last) : "0";
    }

    private static Map<String, Object> fallbackPayload(DataSourceQuery query) {
        int overdueCount = 0;
        if (query.idCardNumber() != null && !query.idCardNumber().isBlank()) {
            char last = query.idCardNumber().charAt(query.idCardNumber().length() - 1);
            overdueCount = Character.getNumericValue(last) % 4;
        }
        return Map.of(
                "credit_overdue_count", overdueCount,
                "credit_query_count_30d", overdueCount > 2 ? 8 : 2
        );
    }
}
