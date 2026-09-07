package com.example.zongshe1.service;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.RiskReport;

import java.util.List;
import java.util.Map;

/**
 * 风控服务接口
 * 
 * <p>该接口为后续开发预留扩展点，提供统一的风控服务入口，整合各种风控规则评估器</p>
 * 
 * <p>主要功能：
 * <ul>
 *   <li>整合多个风控规则评估器，统一执行风险评估</li>
 *   <li>生成风控报告并持久化</li>
 *   <li>提供风控报告查询接口</li>
 *   <li>支持风控规则的动态配置和管理</li>
 * </ul>
 * </p>
 * 
 * @author 开发团队
 * @since 2025-12-21
 */
public interface RiskService {

    /**
     * 执行完整的风险评估流程
     * 
     * <p>整合所有启用的风控规则评估器，对贷款申请进行综合风险评估</p>
     * 
     * @param applicationId 贷款申请ID
     * @return 风险评估结果，包含：
     *         <ul>
     *           <li>overallRiskScore: 综合风险评分</li>
     *           <li>overallRiskLevel: 综合风险等级</li>
     *           <li>passed: 是否通过风控检查</li>
     *           <li>rejectReason: 拒绝原因（如果未通过）</li>
     *           <li>ruleResults: 各规则评估结果列表</li>
     *           <li>riskReportId: 风控报告ID</li>
     *         </ul>
     */
    Map<String, Object> performRiskAssessment(Long applicationId);

    /**
     * 根据申请ID查询风控报告
     * 
     * @param applicationId 贷款申请ID
     * @return 风控报告实体
     */
    RiskReport getRiskReportByApplicationId(Long applicationId);

    /**
     * 获取所有启用的风控规则评估器
     * 
     * @return 评估器列表
     */
    List<RiskRuleEvaluator> getEnabledEvaluators();

    /**
     * 手动触发风险评估（用于定时任务或手动审核）
     * 
     * @param applicationId 贷款申请ID
     * @return 风险评估结果
     */
    Map<String, Object> triggerRiskAssessment(Long applicationId);

    /**
     * 批量执行风险评估
     * 
     * <p>用于批量处理或定时任务场景</p>
     * 
     * @param applicationIds 贷款申请ID列表
     * @return 批量评估结果
     */
    Map<String, Object> batchRiskAssessment(List<Long> applicationIds);

    /**
     * 获取贷款申请的完整风控评估详情（无报告时自动触发评估）。
     */
    Map<String, Object> getRiskAssessmentDetail(Long applicationId);
}
