package com.example.zongshe1.service;

import com.example.zongshe1.entity.User;
import com.example.zongshe1.entity.LoanApplication;

/**
 * 信誉分计算策略接口
 * 
 * <p>该接口为后续开发预留扩展点，支持多种信誉分计算策略：
 * <ul>
 *   <li>基础信誉分计算（当前实现）</li>
 *   <li>基于历史还款记录的信誉分计算</li>
 *   <li>基于多维度数据的综合信誉分计算</li>
 *   <li>基于机器学习的动态信誉分计算</li>
 * </ul>
 * </p>
 * 
 * <p>实现方式：通过实现此接口并注册为Spring Bean，系统会自动注入并使用新的计算策略</p>
 * 
 * @author 开发团队
 * @since 2025-12-21
 */
public interface CreditScoreCalculator {

    /**
     * 计算用户初始信誉分
     * 
     * <p>在用户注册时调用，根据用户基本信息计算初始信誉分</p>
     * 
     * @param user 用户实体（包含身份证号、手机号等基本信息）
     * @return 计算得出的初始信誉分
     */
    Integer calculateInitialCreditScore(User user);

    /**
     * 重新计算用户信誉分
     * 
     * <p>根据用户的历史行为、还款记录、贷款记录等多维度数据重新计算信誉分</p>
     * 
     * @param userId 用户ID
     * @return 重新计算后的信誉分
     */
    Integer recalculateCreditScore(String userId);

    /**
     * 根据贷款申请更新用户信誉分
     * 
     * <p>在贷款申请提交、审核通过、放款、还款等关键节点调用，动态调整用户信誉分</p>
     * 
     * @param userId 用户ID
     * @param application 贷款申请实体
     * @param eventType 事件类型（SUBMIT-提交申请, APPROVE-审核通过, DISBURSE-放款, REPAY-还款, OVERDUE-逾期等）
     * @return 更新后的信誉分
     */
    Integer updateCreditScoreByLoanEvent(String userId, LoanApplication application, String eventType);

    /**
     * 获取计算策略名称
     * 
     * @return 策略名称（如：基础计算策略、历史记录策略、机器学习策略等）
     */
    String getStrategyName();

    /**
     * 获取策略优先级
     * 
     * <p>当存在多个实现时，优先级高的策略会被优先使用</p>
     * 
     * @return 优先级（数字越大优先级越高）
     */
    default int getPriority() {
        return 0;
    }
}
