package com.example.zongshe1.service.scoring;

import java.util.List;
import java.util.Map;

/**
 * 单次评分卡运行结果（不含与现有风控字段的换算，换算在 {@link com.example.zongshe1.service.impl.ScoringCardRiskEvaluator} 中完成）。
 */
public record ScoringCardOutcome(
        int totalPoints,
        int maxPoints,
        String cardVersion,
        List<Map<String, Object>> breakdown
) {
}
