package com.example.zongshe1.service.impl;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.service.RiskRuleEvaluator;
import com.example.zongshe1.service.scoring.ScoringCardEngine;
import com.example.zongshe1.service.scoring.ScoringCardOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 将评分卡引擎接入统一风控链路：卡分越高风险分越低；未达阈值则拒绝。
 */
@Component
@Slf4j
@Order(20)
@RequiredArgsConstructor
public class ScoringCardRiskEvaluator implements RiskRuleEvaluator {

    private final ScoringCardEngine scoringCardEngine;

    @Value("${risk.scoring-card.enabled:true}")
    private boolean enabled;

    @Value("${risk.scoring-card.pass-min-points:40}")
    private int passMinPoints;

    @Override
    public Map<String, Object> evaluateRisk(User user, LoanApplication application) {
        ScoringCardOutcome outcome = scoringCardEngine.evaluate(user, application);

        int max = Math.max(1, outcome.maxPoints());
        int total = outcome.totalPoints();
        int riskScore = Math.min(100, Math.max(0, (int) Math.round(100.0 * (max - total) / max)));
        int riskLevel = riskLevelFromScore(riskScore);
        boolean passed = total >= passMinPoints;

        Map<String, Object> result = new HashMap<>();
        result.put("riskScore", riskScore);
        result.put("riskLevel", riskLevel);
        result.put("passed", passed);
        result.put("reason", passed ? "评分卡通过" : String.format("评分卡未达阈值：得分 %d / %d（要求 ≥ %d）", total, max, passMinPoints));
        result.put("details", Map.of(
                "cardVersion", outcome.cardVersion(),
                "totalPoints", total,
                "maxPoints", max,
                "breakdown", outcome.breakdown()
        ));

        log.info("评分卡评估：userId={}, applicationId={}, totalPoints={}/{}, riskScore={}, passed={}",
                user.getUserId(), application.getId(), total, max, riskScore, passed);
        return result;
    }

    private static int riskLevelFromScore(int riskScore) {
        if (riskScore < 30) {
            return 1;
        }
        if (riskScore < 60) {
            return 2;
        }
        if (riskScore < 80) {
            return 3;
        }
        return 4;
    }

    @Override
    public String getRuleName() {
        return "申请评分卡（演示版 · 双维硬编码分箱）";
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
