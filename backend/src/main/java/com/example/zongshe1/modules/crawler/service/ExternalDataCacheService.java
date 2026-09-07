package com.example.zongshe1.modules.crawler.service;

import com.example.zongshe1.modules.crawler.entity.ExternalDataCache;
import com.example.zongshe1.modules.crawler.repository.ExternalDataCacheRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExternalDataCacheService {

    private final ExternalDataCacheRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> getPayload(String sourceType, String idCard, String phone) {
        LocalDateTime now = LocalDateTime.now();
        Optional<ExternalDataCache> row = repository
                .findFirstBySourceTypeAndIdCardNumberAndPhoneNumberAndExpiresAtAfterOrderByCrawledAtDesc(
                        sourceType, nullSafe(idCard), nullSafe(phone), now);
        if (row.isEmpty() && idCard != null) {
            row = repository.findFirstBySourceTypeAndExpiresAtAfterOrderByCrawledAtDesc(sourceType, now);
        }
        return row.flatMap(this::parsePayload);
    }

    @Transactional
    public void save(String sourceType, String idCard, String phone, Map<String, Object> payload,
                     String sourceUrl, int ttlHours) {
        ExternalDataCache cache = new ExternalDataCache();
        cache.setSourceType(sourceType);
        cache.setIdCardNumber(nullSafe(idCard));
        cache.setPhoneNumber(nullSafe(phone));
        cache.setPayloadJson(writeJson(payload));
        cache.setCrawlSourceUrl(sourceUrl);
        cache.setCrawledAt(LocalDateTime.now());
        cache.setExpiresAt(LocalDateTime.now().plusHours(ttlHours));
        repository.save(cache);
    }

    private Optional<Map<String, Object>> parsePayload(ExternalDataCache cache) {
        try {
            return Optional.of(objectMapper.readValue(cache.getPayloadJson(), new TypeReference<>() {}));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("序列化爬取数据失败", e);
        }
    }

    private static String nullSafe(String v) {
        return v == null ? "" : v;
    }
}
