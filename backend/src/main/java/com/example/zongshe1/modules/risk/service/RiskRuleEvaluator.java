package com.example.zongshe1.service;

import com.example.zongshe1.entity.User;
import com.example.zongshe1.entity.LoanApplication;

import java.util.Map;

/**
 * 风控规则评估器接口
 * 
 * <p>该接口为后续开发预留扩展点，支持多种风控规则评估策略：
 * <ul>
 *   <li>基础风控规则（信用分检查、用户状态检查等）</li>
 *   <li>反欺诈规则（设备指纹、IP地址、行为异常检测等）</li>
 *   <li>额度风控规则（单笔额度、累计额度、还款能力评估等）</li>
 *   <li>黑名单规则（用户黑名单、设备黑名单、IP黑名单等）</li>
 *   <li>实时风控规则（实时数据流分析、异常行为检测等）</li>
 * </ul>
 * </p>
 * 
 * <p>实现方式：通过实现此接口并注册为Spring Bean，系统会在贷款申请审核时自动调用所有评估器</p>
 * 
 * @author 开发团队
 * @since 2025-12-21
 */
public interface RiskRuleEvaluator {

    /**
     * 评估贷款申请的风控风险
     * 
     * <p>对贷款申请进行风险评估，返回风险评分和风险等级</p>
     * 
     * @param user 用户实体
     * @param application 贷款申请实体
     * @return 风险评估结果，包含：
     *         <ul>
     *           <li>riskScore: 风险评分（0-100，分数越高风险越大）</li>
     *           <li>riskLevel: 风险等级（1-低风险, 2-中风险, 3-高风险, 4-极高风险）</li>
     *           <li>passed: 是否通过风控检查（true-通过, false-拒绝）</li>
     *           <li>reason: 拒绝原因（如果未通过）</li>
     *           <li>details: 详细评估信息（可选）</li>
     *         </ul>
     */
    Map<String, Object> evaluateRisk(User user, LoanApplication application);

    /**
     * 获取规则名称
     * 
     * @return 规则名称（如：基础风控规则、反欺诈规则、额度风控规则等）
     */
    String getRuleName();

    /**
     * 获取规则优先级
     * 
     * <p>当存在多个评估器时，按优先级顺序执行评估</p>
     * 
     * @return 优先级（数字越小优先级越高，先执行）
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 判断该规则是否启用
     * 
     * <p>可以通过配置控制某个规则是否启用</p>
     * 
     * @return true-启用, false-禁用
     */
    default boolean isEnabled() {
        return true;
    }
}
