package com.example.zongshe1.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RiskReasonCode {
    CREDIT_LOW("CREDIT_LOW", "信用分偏低"),
    OVERDUE_HIGH("OVERDUE_HIGH", "近6个月逾期偏高"),
    DISHONEST_PUBLIC("DISHONEST_PUBLIC", "失信公示命中"),
    LPR_HIGH("LPR_HIGH", "LPR 环境偏高"),
    LPR_OK("LPR_OK", "LPR 环境正常"),
    FAST_TRACK("FAST_TRACK", "快速通道放行"),
    MANUAL_REVIEW("MANUAL_REVIEW", "需人工复核");

    private final String code;
    private final String description;
}
