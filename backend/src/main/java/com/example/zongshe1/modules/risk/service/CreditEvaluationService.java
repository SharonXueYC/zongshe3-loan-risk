package com.example.zongshe1.modules.risk.service;

import java.util.Map;

/**
 * 用户信誉分综合评估服务。
 */
public interface CreditEvaluationService {

    /**
     * 重新计算并返回用户信誉分评估结果（含额度、等级、因子明细）。
     */
    Map<String, Object> evaluateCreditScore(String userId);

    /**
     * 提交增信资料并重新评估额度。
     */
    Map<String, Object> submitCreditDocuments(String userId, java.util.List<String> documentTypes);
}
