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

@Component
@RequiredArgsConstructor
public class MockTelecomAdapter implements ExternalDataSourceAdapter {

    private final ExternalDataCacheService cacheService;

    @Value("${datasource.telecom.enabled:true}")
    private boolean enabled;

    @Value("${datasource.telecom.timeout-ms:2000}")
    private long timeoutMs;

    @Override
    public DataSourceType getSourceType() {
        return DataSourceType.TELECOM;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public long getTimeoutMs() {
        return timeoutMs;
    }

    @Override
    public ExternalDataFetchResult fetch(DataSourceQuery query) {
        String suffix = extractPhoneSuffix(query.phoneNumber());
        var cached = cacheService.getPayload(
                DataSourceType.TELECOM.name(), "", "SUFFIX_" + suffix);
        if (cached.isPresent()) {
            return ExternalDataFetchResult.ok(getSourceType(), cached.get());
        }
        return ExternalDataFetchResult.ok(getSourceType(), fallbackPayload(query));
    }

    private static String extractPhoneSuffix(String phone) {
        if (phone == null || phone.length() < 1) {
            return "0";
        }
        char last = phone.charAt(phone.length() - 1);
        return Character.isDigit(last) ? String.valueOf(last) : "0";
    }

    private static Map<String, Object> fallbackPayload(DataSourceQuery query) {
        int onlineMonths = 12;
        if (query.phoneNumber() != null && query.phoneNumber().length() >= 4) {
            onlineMonths = 6 + (query.phoneNumber().charAt(3) % 60);
        }
        return Map.of(
                "telecom_online_months", onlineMonths,
                "telecom_real_name_verified", true
        );
    }
}
