package com.example.zongshe1.service.impl;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.modules.risk.feature.CurrentRiskFeatures;
import com.example.zongshe1.modules.risk.feature.FeatureSnapshot;
import com.example.zongshe1.service.RiskRuleEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于外部数据源变量的风控规则（演示：征信逾期、关键源失败）。
 */
@Component
@Order(10)
@RequiredArgsConstructor
public class ExternalDataRiskEvaluator implements RiskRuleEvaluator {

    private final CurrentRiskFeatures currentRiskFeatures;

    @Value("${datasource.risk-rule.enabled:true}")
    private boolean enabled;

    @Value("${datasource.risk-rule.max-overdue-count:2}")
    private int maxOverdueCount;

    @Override
    public Map<String, Object> evaluateRisk(User user, LoanApplication application) {
        Map<String, Object> result = new HashMap<>();

        if (!currentRiskFeatures.isReady()) {
            result.put("riskScore", 50);
            result.put("riskLevel", 2);
            result.put("passed", true);
            result.put("reason", "外部特征未加载，跳过外部数据规则");
            return result;
        }

        FeatureSnapshot snapshot = currentRiskFeatures.get();

        if (Boolean.TRUE.equals(snapshot.get("required_source_failed"))) {
            result.put("riskScore", 95);
            result.put("riskLevel", 4);
            result.put("passed", false);
            result.put("reason", "关键外部数据源不可用");
            return result;
        }

        int overdue = snapshot.getInt("credit_overdue_count", 0);
        if (overdue > maxOverdueCount) {
            result.put("riskScore", 90);
            result.put("riskLevel", 4);
            result.put("passed", false);
            result.put("reason", String.format("征信逾期次数过高：%d（阈值 %d）", overdue, maxOverdueCount));
            return result;
        }

        result.put("riskScore", 30);
        result.put("riskLevel", 1);
        result.put("passed", true);
        result.put("reason", "外部数据规则通过");
        result.put("details", Map.of(
                "credit_overdue_count", overdue,
                "telecom_online_months", snapshot.getInt("telecom_online_months", 0)
        ));
        return result;
    }

    @Override
    public String getRuleName() {
        return "外部数据源规则（征信/运营商演示）";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
