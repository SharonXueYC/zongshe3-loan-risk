package com.example.zongshe1.common.dto;

import com.example.zongshe1.common.enums.RiskReasonCode;
import com.example.zongshe1.common.dto.FeatureSnapshotDTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class RiskReportDTO {

    private Long reportId;
    private Long applicationId;
    private Long userId;
    private String decision;
    private Integer totalScore;
    private Integer maxScore;
    private String cardVersion;
    private String reasonSummary;
    private List<RiskReasonCode> reasonCodes;
    private Map<String, Object> breakdown;
    private FeatureSnapshotDTO featureSnapshot;
    private Boolean stale;
    private LocalDateTime assessedAt;
}
