package com.example.zongshe1.modules.risk.feature;

import com.example.zongshe1.common.dto.FeatureSnapshotDTO;
import com.example.zongshe1.modules.crawler.service.PublicSourceDemoCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PublicFeatureSnapshotAssemblerService {

    private final PublicSourceDemoCrawlerService publicSourceDemoCrawlerService;

    public FeatureSnapshotDTO assembleForUser(Long userId) {
        FeatureSnapshotDTO dto = new FeatureSnapshotDTO();
        dto.setUserId(userId);
        dto.setAge(30);
        dto.setCreditScore(650);
        dto.setStale(true);

        Map<String, Object> snapshot = publicSourceDemoCrawlerService.fetchDemoSnapshot();
        Map<String, Object> dishonest = asMap(snapshot.get("dishonest"));
        Map<String, Object> lpr = asMap(snapshot.get("lpr"));

        if (dishonest != null) {
            Object dishonestHit = dishonest.get("dishonestHit");
            Object dishonestName = dishonest.get("dishonestName");
            Object courtName = dishonest.get("courtName");
            Object publishDate = dishonest.get("publishDate");
            Object caseNumber = dishonest.get("caseNumber");
            Object sourceUrl = dishonest.get("sourceUrl");

            dto.setDishonestHit(Boolean.TRUE.equals(dishonestHit));
            dto.setDishonestName(dishonestName == null ? null : String.valueOf(dishonestName));
            dto.setCourtName(courtName == null ? null : String.valueOf(courtName));
            dto.setPublishDate(parseToLocalDateTime(publishDate));
            dto.setCaseNumber(caseNumber == null ? null : String.valueOf(caseNumber));
            dto.setSourceUrl(sourceUrl == null ? null : String.valueOf(sourceUrl));
            dto.setExtra(caseNumber == null ? null : Map.of("caseNumber", String.valueOf(caseNumber)));
            dto.setStale(false);
        }

        if (lpr != null) {
            Object lpr1y = lpr.get("lpr1y");
            Object lpr5y = lpr.get("lpr5y");
            Object lprPublishDate = lpr.get("lprPublishDate");
            Object sourceUrl = lpr.get("sourceUrl");

            if (lpr1y != null) {
                dto.setLpr1y(new BigDecimal(String.valueOf(lpr1y)));
            }
            if (lpr5y != null) {
                dto.setLpr5y(new BigDecimal(String.valueOf(lpr5y)));
            }
            dto.setLprPublishDate(parseToLocalDateTime(lprPublishDate));
            dto.setSourceUrl(sourceUrl == null ? dto.getSourceUrl() : String.valueOf(sourceUrl));
            dto.setStale(false);
        }

        return dto;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private LocalDateTime parseToLocalDateTime(Object value) {
        if (value == null) {
            return LocalDateTime.now();
        }
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            if (raw.contains("-")) {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            }
            if (raw.contains("年")) {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy年M月d日")).atStartOfDay();
            }
        } catch (Exception ignored) {
            // ignore and fallback to now
        }
        return LocalDateTime.now();
    }
}
