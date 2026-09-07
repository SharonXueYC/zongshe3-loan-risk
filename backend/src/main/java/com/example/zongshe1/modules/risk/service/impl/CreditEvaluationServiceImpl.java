package com.example.zongshe1.modules.risk.service.impl;

import com.example.zongshe1.entity.User;
import com.example.zongshe1.modules.risk.service.CreditEvaluationService;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.service.CreditScoreCalculator;
import com.example.zongshe1.service.impl.DefaultCreditScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreditEvaluationServiceImpl implements CreditEvaluationService {

    private final UserRepository userRepository;
    private final List<CreditScoreCalculator> creditScoreCalculators;

    @Value("${app.system.min-credit-score-for-loan:450}")
    private Integer minCreditScoreForLoan;

    @Value("${app.system.initial-loan-limit:5000}")
    private Integer initialLoanLimit;

    @Override
    public Map<String, Object> evaluateCreditScore(String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            CreditScoreCalculator calculator = getCalculator();
            boolean hasIdentity = user.getIdCardNumber() != null && !user.getIdCardNumber().isBlank();

            int creditScore;
            int loanLimit;
            if (!hasIdentity) {
                creditScore = user.getCreditScore() != null ? user.getCreditScore() : 500;
                loanLimit = initialLoanLimit;
            } else {
                Integer newScore = calculator.recalculateCreditScore(userId);
                creditScore = newScore != null ? newScore : (user.getCreditScore() != null ? user.getCreditScore() : 500);
                loanLimit = calculateLoanLimit(creditScore);
            }
            String creditLevel = creditLevelLabel(creditScore);
            boolean qualified = creditScore >= minCreditScoreForLoan && user.getUserStatus() == 1;

            List<Map<String, Object>> factors = List.of();
            if (calculator instanceof DefaultCreditScoreCalculator defaultCalculator) {
                factors = defaultCalculator.buildEvaluationFactors(userId);
            }

            User latestUser = userRepository.findByUserId(userId).orElse(user);
            result.put("success", true);
            result.put("userId", userId);
            result.put("creditScore", creditScore);
            result.put("maxCreditScore", 850);
            result.put("creditLevel", creditLevel);
            result.put("loanLimit", loanLimit);
            result.put("qualified", qualified);
            result.put("strategyName", calculator.getStrategyName());
            result.put("factors", factors);
            result.put("submittedDocuments", new java.util.ArrayList<>(
                    DefaultCreditScoreCalculator.parseSubmittedDocuments(latestUser.getCreditDocuments())));

            log.info("信誉分评估完成：userId={}, score={}, limit={}", userId, creditScore, loanLimit);
        } catch (Exception e) {
            log.error("信誉分评估失败：userId={}", userId, e);
            result.put("success", false);
            result.put("message", "信誉分评估失败：" + e.getMessage());
        }
        return result;
    }

    private CreditScoreCalculator getCalculator() {
        return creditScoreCalculators.stream()
                .min((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .orElseThrow(() -> new RuntimeException("未找到信誉分计算策略"));
    }

    private static int calculateLoanLimit(int creditScore) {
        if (creditScore < 500) {
            return 0;
        }
        int baseLimit = 5000;
        int creditBonus = (creditScore - 500) / 10 * 1000;
        return Math.min(baseLimit + creditBonus, 500000);
    }

    private static String creditLevelLabel(int creditScore) {
        if (creditScore >= 750) {
            return "优秀";
        }
        if (creditScore >= 650) {
            return "良好";
        }
        if (creditScore >= 550) {
            return "一般";
        }
        if (creditScore >= 450) {
            return "偏低";
        }
        return "不足";
    }

    @Override
    public Map<String, Object> submitCreditDocuments(String userId, List<String> documentTypes) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            if (documentTypes == null || documentTypes.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要提交的资料类型");
                return result;
            }

            Set<String> merged = new LinkedHashSet<>(DefaultCreditScoreCalculator.parseSubmittedDocuments(user.getCreditDocuments()));
            Set<String> validTypes = Set.of("education", "income", "property", "social");
            for (String doc : documentTypes) {
                if (doc == null) {
                    continue;
                }
                String key = doc.trim();
                if (validTypes.contains(key)) {
                    merged.add(key);
                }
            }
            if (merged.isEmpty()) {
                result.put("success", false);
                result.put("message", "资料类型无效，可选：education、income、property、social");
                return result;
            }

            user.setCreditDocuments(String.join(",", merged));
            userRepository.save(user);

            Map<String, Object> evaluation = evaluateCreditScore(userId);
            evaluation.put("message", "资料已提交，额度已更新");
            return evaluation;
        } catch (Exception e) {
            log.error("提交增信资料失败：userId={}", userId, e);
            result.put("success", false);
            result.put("message", "提交失败：" + e.getMessage());
            return result;
        }
    }
}
