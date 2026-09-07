package com.example.zongshe1.service.impl;

import com.example.zongshe1.entity.User;
import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.service.RiskRuleEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 基础风控规则评估器实现
 * 
 * <p>当前实现基础的信用分和用户状态检查，后续可以扩展更多规则</p>
 * 
 * <p>扩展方式：
 * <ol>
 *   <li>创建新的实现类，实现RiskRuleEvaluator接口</li>
 *   <li>使用@Order注解设置优先级</li>
 *   <li>系统会自动注入所有实现，按优先级顺序执行</li>
 * </ol>
 * </p>
 * 
 * @author 开发团队
 * @since 2025-12-21
 */
@Component
@Slf4j
@Order(1) // 基础规则优先级最高，最先执行
public class BasicRiskRuleEvaluator implements RiskRuleEvaluator {

    @Value("${app.system.min-credit-score-for-loan:450}")
    private Integer minCreditScoreForLoan;

    @Override
    public Map<String, Object> evaluateRisk(User user, LoanApplication application) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 检查信用分是否达标
        if (user.getCreditScore() < minCreditScoreForLoan) {
            result.put("riskScore", 80); // 高风险
            result.put("riskLevel", 3); // 高风险
            result.put("passed", false);
            result.put("reason", String.format("信用分不足，当前信用分：%d，最低要求：%d", 
                    user.getCreditScore(), minCreditScoreForLoan));
            log.warn("基础风控规则：信用分不足 - userId={}, creditScore={}, minRequired={}", 
                    user.getUserId(), user.getCreditScore(), minCreditScoreForLoan);
            return result;
        }

        // 2. 检查用户状态
        if (user.getUserStatus() != 1) { // 不是正常状态
            result.put("riskScore", 90); // 极高风险
            result.put("riskLevel", 4); // 极高风险
            result.put("passed", false);
            result.put("reason", String.format("用户状态异常，当前状态：%d", user.getUserStatus()));
            log.warn("基础风控规则：用户状态异常 - userId={}, status={}", 
                    user.getUserId(), user.getUserStatus());
            return result;
        }

        // 3. 根据信用分计算风险评分
        int riskScore = calculateRiskScoreByCreditScore(user.getCreditScore());
        int riskLevel = calculateRiskLevel(riskScore);

        result.put("riskScore", riskScore);
        result.put("riskLevel", riskLevel);
        result.put("passed", true);
        result.put("reason", "通过基础风控检查");
        
        log.info("基础风控规则评估完成 - userId={}, creditScore={}, riskScore={}, riskLevel={}", 
                user.getUserId(), user.getCreditScore(), riskScore, riskLevel);
        
        return result;
    }

    /**
     * 根据信用分计算风险评分
     * 
     * <p>信用分越高，风险评分越低</p>
     * 
     * @param creditScore 信用分
     * @return 风险评分（0-100）
     */
    private int calculateRiskScoreByCreditScore(Integer creditScore) {
        if (creditScore >= 700) {
            return 20; // 低风险
        } else if (creditScore >= 600) {
            return 40; // 中低风险
        } else if (creditScore >= 500) {
            return 60; // 中等风险
        } else if (creditScore >= 450) {
            return 75; // 中高风险
        } else {
            return 90; // 高风险
        }
    }

    /**
     * 根据风险评分计算风险等级
     * 
     * @param riskScore 风险评分
     * @return 风险等级（1-低风险, 2-中风险, 3-高风险, 4-极高风险）
     */
    private int calculateRiskLevel(int riskScore) {
        if (riskScore < 30) {
            return 1; // 低风险
        } else if (riskScore < 60) {
            return 2; // 中风险
        } else if (riskScore < 80) {
            return 3; // 高风险
        } else {
            return 4; // 极高风险
        }
    }

    @Override
    public String getRuleName() {
        return "基础风控规则（信用分和用户状态检查）";
    }

    @Override
    public int getPriority() {
        return 1; // 基础规则优先级最高
    }

    @Override
    public boolean isEnabled() {
        return true; // 默认启用
    }
}
