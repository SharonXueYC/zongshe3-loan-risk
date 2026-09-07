package com.example.zongshe1.service.scoring.impl;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.modules.risk.feature.CurrentRiskFeatures;
import com.example.zongshe1.modules.risk.feature.FeatureSnapshot;
import com.example.zongshe1.service.scoring.ScoringCardEngine;
import com.example.zongshe1.service.scoring.ScoringCardOutcome;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 进度演示用评分卡：硬编码两维特征（信用分、申请金额），后续可改为表驱动或 YAML。
 */
@Component
@RequiredArgsConstructor
public class SimpleScoringCardEngine implements ScoringCardEngine {

    public static final int MAX_POINTS = 100;

    private final CurrentRiskFeatures currentRiskFeatures;

    @Value("${risk.scoring-card.version:demo-v1}")
    private String cardVersion;

    @Override
    public ScoringCardOutcome evaluate(User user, LoanApplication application) {
        List<Map<String, Object>> breakdown = new ArrayList<>();
        FeatureSnapshot snapshot = currentRiskFeatures.isReady()
                ? currentRiskFeatures.get()
                : null;

        int credit = snapshot != null
                ? snapshot.getInt("credit_score", 0)
                : (user.getCreditScore() != null ? user.getCreditScore() : 0);
        int creditPts = pointsForCreditScore(credit);
        breakdown.add(line("credit_score", credit, binCreditLabel(credit), creditPts));

        BigDecimal amount = snapshot != null && snapshot.get("loan_amount") instanceof BigDecimal bd
                ? bd
                : (application.getLoanAmount() != null ? application.getLoanAmount() : BigDecimal.ZERO);
        int amountPts = pointsForLoanAmount(amount);
        breakdown.add(line("loan_amount", amount, binAmountLabel(amount), amountPts));

        int total = creditPts + amountPts;

        if (snapshot != null) {
            int overdue = snapshot.getInt("credit_overdue_count", 0);
            int overduePts = pointsForOverdue(overdue);
            breakdown.add(line("credit_overdue_count", overdue, "逾期" + overdue + "次", overduePts));
            total += overduePts;

            int onlineMonths = snapshot.getInt("telecom_online_months", 0);
            int telecomPts = pointsForOnlineMonths(onlineMonths);
            breakdown.add(line("telecom_online_months", onlineMonths, onlineMonths + "个月", telecomPts));
            total += telecomPts;
        }

        total = Math.min(MAX_POINTS, total);
        return new ScoringCardOutcome(total, MAX_POINTS, cardVersion, breakdown);
    }

    private static int pointsForOverdue(int overdueCount) {
        if (overdueCount == 0) {
            return 10;
        }
        if (overdueCount <= 2) {
            return 0;
        }
        return -20;
    }

    private static int pointsForOnlineMonths(int months) {
        if (months >= 24) {
            return 10;
        }
        if (months >= 12) {
            return 5;
        }
        return 0;
    }

    private static Map<String, Object> line(String code, Object raw, String binLabel, int points) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("featureCode", code);
        m.put("rawValue", raw);
        m.put("binLabel", binLabel);
        m.put("points", points);
        return m;
    }

    private static int pointsForCreditScore(int creditScore) {
        if (creditScore >= 700) {
            return 50;
        }
        if (creditScore >= 600) {
            return 35;
        }
        if (creditScore >= 500) {
            return 20;
        }
        return 5;
    }

    private static String binCreditLabel(int creditScore) {
        if (creditScore >= 700) {
            return "[700,+∞)";
        }
        if (creditScore >= 600) {
            return "[600,700)";
        }
        if (creditScore >= 500) {
            return "[500,600)";
        }
        return "(-∞,500)";
    }

    private static int pointsForLoanAmount(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("50000")) <= 0) {
            return 30;
        }
        if (amount.compareTo(new BigDecimal("200000")) <= 0) {
            return 20;
        }
        return 10;
    }

    private static String binAmountLabel(BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("50000")) <= 0) {
            return "(0, 5万]";
        }
        if (amount.compareTo(new BigDecimal("200000")) <= 0) {
            return "(5万, 20万]";
        }
        return "(20万,+∞)";
    }
}
