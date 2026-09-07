package com.example.zongshe1.service.impl;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.repository.LoanApplicationRepository;
import com.example.zongshe1.repository.RepayPlanRepository;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.service.CreditScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * 默认信誉分计算策略：基于用户信息、贷款历史与还款行为综合评分。
 */
@Component
@Slf4j
@Order(100)
@RequiredArgsConstructor
public class DefaultCreditScoreCalculator implements CreditScoreCalculator {

    private static final int MIN_SCORE = 300;
    private static final int MAX_SCORE = 850;

    private final UserRepository userRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final RepayPlanRepository repayPlanRepository;

    @Value("${app.system.default-credit-score:500}")
    private Integer defaultCreditScore;

    private static final Map<String, DocumentBonus> DOCUMENT_BONUSES = Map.of(
            "education", new DocumentBonus("学历证明", 10),
            "income", new DocumentBonus("收入证明", 15),
            "property", new DocumentBonus("房产证明", 20),
            "social", new DocumentBonus("社保缴纳证明", 8)
    );

    private record DocumentBonus(String label, int points) {}

    @Override
    public Integer calculateInitialCreditScore(User user) {
        int score = defaultCreditScore;
        if (user.getIdCardNumber() != null && !user.getIdCardNumber().isBlank()) {
            score += 15;
        }
        if (user.getUserName() != null && !user.getUserName().isBlank()) {
            score += 10;
        }
        score = clamp(score);
        log.debug("计算初始信誉分：userId={}, score={}", user.getUserId(), score);
        return score;
    }

    @Override
    public Integer recalculateCreditScore(String userId) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            return null;
        }

        int score = defaultCreditScore;
        List<Map<String, Object>> factors = new ArrayList<>();

        if (user.getIdCardNumber() != null && !user.getIdCardNumber().isBlank()) {
            score += 15;
            factors.add(factor("实名认证", 15, "已完成身份证实名"));
        }
        if (user.getUserName() != null && !user.getUserName().isBlank()) {
            score += 10;
            factors.add(factor("资料完整", 10, "已完善个人姓名"));
        }

        List<LoanApplication> applications = loanApplicationRepository.findByUserIdOrderByApplyTimeDesc(userId);
        long approvedCount = applications.stream().filter(a -> "approved".equals(a.getStatus())).count();
        long rejectedCount = applications.stream().filter(a -> "rejected".equals(a.getStatus())).count();
        int approvedBonus = (int) approvedCount * 8;
        int rejectedPenalty = (int) rejectedCount * 5;
        score += approvedBonus - rejectedPenalty;
        if (approvedBonus > 0) {
            factors.add(factor("历史获批", approvedBonus, "累计获批 " + approvedCount + " 笔"));
        }
        if (rejectedPenalty > 0) {
            factors.add(factor("历史拒贷", -rejectedPenalty, "累计被拒 " + rejectedCount + " 笔"));
        }

        long paidCount = repayPlanRepository.countPaidByUserId(userId);
        long overdueCount = repayPlanRepository.countOverdueByUserId(userId);
        int repayBonus = (int) paidCount * 3;
        int overduePenalty = (int) overdueCount * 12;
        score += repayBonus - overduePenalty;
        if (repayBonus > 0) {
            factors.add(factor("按时还款", repayBonus, "已还清 " + paidCount + " 期"));
        }
        if (overduePenalty > 0) {
            factors.add(factor("逾期记录", -overduePenalty, "逾期 " + overdueCount + " 期"));
        }

        score += applyDocumentBonuses(user, factors);
        score = clamp(score);
        user.setCreditScore(score);
        userRepository.save(user);

        log.info("重新计算信誉分：userId={}, score={}, factors={}", userId, score, factors.size());
        return score;
    }

    /**
     * 构建信誉分评估因子明细（供信用评估 API 返回）。
     */
    public List<Map<String, Object>> buildEvaluationFactors(String userId) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            return List.of();
        }

        List<Map<String, Object>> factors = new ArrayList<>();
        if (user.getIdCardNumber() != null && !user.getIdCardNumber().isBlank()) {
            factors.add(factor("实名认证", 15, "已完成身份证实名"));
        }
        if (user.getUserName() != null && !user.getUserName().isBlank()) {
            factors.add(factor("资料完整", 10, "已完善个人姓名"));
        }

        List<LoanApplication> applications = loanApplicationRepository.findByUserIdOrderByApplyTimeDesc(userId);
        long approvedCount = applications.stream().filter(a -> "approved".equals(a.getStatus())).count();
        long rejectedCount = applications.stream().filter(a -> "rejected".equals(a.getStatus())).count();
        if (approvedCount > 0) {
            factors.add(factor("历史获批", (int) approvedCount * 8, "累计获批 " + approvedCount + " 笔"));
        }
        if (rejectedCount > 0) {
            factors.add(factor("历史拒贷", -(int) rejectedCount * 5, "累计被拒 " + rejectedCount + " 笔"));
        }

        long paidCount = repayPlanRepository.countPaidByUserId(userId);
        long overdueCount = repayPlanRepository.countOverdueByUserId(userId);
        if (paidCount > 0) {
            factors.add(factor("按时还款", (int) paidCount * 3, "已还清 " + paidCount + " 期"));
        }
        if (overdueCount > 0) {
            factors.add(factor("逾期记录", -(int) overdueCount * 12, "逾期 " + overdueCount + " 期"));
        }
        applyDocumentBonuses(user, factors);
        return factors;
    }

    /**
     * 解析用户已提交的增信资料类型。
     */
    public static Set<String> parseSubmittedDocuments(String creditDocuments) {
        Set<String> docs = new LinkedHashSet<>();
        if (creditDocuments == null || creditDocuments.isBlank()) {
            return docs;
        }
        for (String part : creditDocuments.split(",")) {
            String key = part.trim();
            if (!key.isEmpty() && DOCUMENT_BONUSES.containsKey(key)) {
                docs.add(key);
            }
        }
        return docs;
    }

    private int applyDocumentBonuses(User user, List<Map<String, Object>> factors) {
        int bonus = 0;
        for (String docKey : parseSubmittedDocuments(user.getCreditDocuments())) {
            DocumentBonus doc = DOCUMENT_BONUSES.get(docKey);
            if (doc == null) {
                continue;
            }
            bonus += doc.points();
            factors.add(factor(doc.label(), doc.points(), "已提交增信资料"));
        }
        return bonus;
    }

    @Override
    public Integer updateCreditScoreByLoanEvent(String userId, LoanApplication application, String eventType) {
        User user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            return null;
        }

        int current = user.getCreditScore() != null ? user.getCreditScore() : defaultCreditScore;
        int delta = switch (eventType != null ? eventType.toUpperCase() : "") {
            case "SUBMIT" -> -2;
            case "APPROVE" -> 8;
            case "REJECT" -> -5;
            case "REPAY" -> 5;
            case "OVERDUE" -> -15;
            default -> 0;
        };
        if (delta == 0) {
            return current;
        }

        int newScore = clamp(current + delta);
        user.setCreditScore(newScore);
        userRepository.save(user);
        log.info("贷款事件更新信誉分：userId={}, event={}, {} -> {}", userId, eventType, current, newScore);
        return newScore;
    }

    @Override
    public String getStrategyName() {
        return "综合信誉分计算策略";
    }

    @Override
    public int getPriority() {
        return 100;
    }

    private static int clamp(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }

    private static Map<String, Object> factor(String name, int points, String description) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("points", points);
        m.put("description", description);
        return m;
    }
}
