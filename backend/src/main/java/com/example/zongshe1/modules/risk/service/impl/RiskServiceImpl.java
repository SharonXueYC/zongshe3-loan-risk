package com.example.zongshe1.service.impl;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.RiskReport;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.repository.LoanApplicationRepository;
import com.example.zongshe1.repository.RiskReportRepository;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.modules.risk.feature.CurrentRiskFeatures;
import com.example.zongshe1.modules.risk.feature.FeatureContextService;
import com.example.zongshe1.modules.risk.feature.FeatureSnapshot;
import com.example.zongshe1.service.RiskRuleEvaluator;
import com.example.zongshe1.service.RiskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 风控服务实现类
 * 
 * <p>整合所有风控规则评估器，提供统一的风控服务</p>
 * 
 * <p>扩展方式：
 * <ul>
 *   <li>系统会自动注入所有实现RiskRuleEvaluator接口的Bean</li>
 *   <li>按优先级顺序执行所有启用的评估器</li>
 *   <li>如果任何一个评估器返回passed=false，则整体评估失败</li>
 *   <li>综合所有评估器的结果，生成最终的风险评分和等级</li>
 * </ul>
 * </p>
 * 
 * @author 开发团队
 * @since 2025-12-21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RiskServiceImpl implements RiskService {

    private final List<RiskRuleEvaluator> riskRuleEvaluators;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final RiskReportRepository riskReportRepository;
    private final FeatureContextService featureContextService;
    private final CurrentRiskFeatures currentRiskFeatures;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Map<String, Object> performRiskAssessment(Long applicationId) {
        log.info("开始执行风险评估：applicationId={}", applicationId);

        // 1. 查询贷款申请和用户信息
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("贷款申请不存在：" + applicationId));
        
        User user = userRepository.findByUserId(application.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        FeatureSnapshot featureSnapshot = featureContextService.build(user, application);
        currentRiskFeatures.set(featureSnapshot);
        try {
            log.debug("特征快照已构建：featureCount={}, sources={}",
                    featureSnapshot.getFeatures().size(),
                    featureSnapshot.getSourceResults().keySet());

            List<RiskRuleEvaluator> enabledEvaluators = getEnabledEvaluators();

            List<Map<String, Object>> ruleResults = new ArrayList<>();
            int maxRiskScore = 0;
            int maxRiskLevel = 1;
            boolean allPassed = true;
            String rejectReason = null;

            for (RiskRuleEvaluator evaluator : enabledEvaluators) {
                try {
                    Map<String, Object> ruleResult = evaluator.evaluateRisk(user, application);
                    ruleResult.put("ruleName", evaluator.getRuleName());
                    ruleResults.add(ruleResult);

                    Boolean passed = (Boolean) ruleResult.get("passed");
                    if (passed != null && !passed) {
                        allPassed = false;
                        if (rejectReason == null) {
                            rejectReason = (String) ruleResult.get("reason");
                        }
                    }

                    Integer riskScore = (Integer) ruleResult.get("riskScore");
                    Integer riskLevel = (Integer) ruleResult.get("riskLevel");
                    if (riskScore != null && riskScore > maxRiskScore) {
                        maxRiskScore = riskScore;
                    }
                    if (riskLevel != null && riskLevel > maxRiskLevel) {
                        maxRiskLevel = riskLevel;
                    }

                    log.debug("风控规则评估完成：ruleName={}, passed={}, riskScore={}, riskLevel={}",
                            evaluator.getRuleName(), passed, riskScore, riskLevel);
                } catch (Exception e) {
                    log.error("执行风控规则评估失败：ruleName={}, error={}",
                            evaluator.getRuleName(), e.getMessage(), e);
                    allPassed = false;
                    if (rejectReason == null) {
                        rejectReason = "风控规则评估异常：" + evaluator.getRuleName();
                    }
                }
            }

            RiskReport riskReport = riskReportRepository.findByLoanApplication(application)
                    .orElseGet(RiskReport::new);
            riskReport.setLoanApplication(application);
            riskReport.setRiskScore(maxRiskScore);
            riskReport.setRiskLevel(maxRiskLevel);
            riskReport.setPassed(allPassed);
            riskReport.setRejectReason(rejectReason);
            extractScoringCardDetails(ruleResults, riskReport);
            riskReport.setDetailsJson(toJson(Map.of(
                    "ruleResults", ruleResults,
                    "featureSnapshot", featureSnapshot.getFeatures(),
                    "dataSourceResults", featureSnapshot.getSourceResults()
            )));
            riskReport = riskReportRepository.save(riskReport);

            Map<String, Object> result = new HashMap<>();
            result.put("overallRiskScore", maxRiskScore);
            result.put("overallRiskLevel", maxRiskLevel);
            result.put("passed", allPassed);
            result.put("rejectReason", rejectReason);
            result.put("ruleResults", ruleResults);
            result.put("riskReportId", riskReport.getId());
            result.put("scoringCardPoints", riskReport.getScoringCardPoints());
            result.put("scoringCardMax", riskReport.getScoringCardMax());
            result.put("cardVersion", riskReport.getCardVersion());
            result.put("evaluatedAt", new Date());
            result.put("featureSnapshot", featureSnapshot.getFeatures());
            result.put("dataSourceResults", featureSnapshot.getSourceResults());

            log.info("风险评估完成：applicationId={}, passed={}, riskScore={}, riskLevel={}, ruleCount={}",
                    applicationId, allPassed, maxRiskScore, maxRiskLevel, ruleResults.size());

            return result;
        } finally {
            currentRiskFeatures.clear();
        }
    }

    @Override
    public RiskReport getRiskReportByApplicationId(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("贷款申请不存在：" + applicationId));
        
        return riskReportRepository.findByLoanApplication(application)
                .orElse(null);
    }

    @Override
    public List<RiskRuleEvaluator> getEnabledEvaluators() {
        return riskRuleEvaluators.stream()
                .filter(RiskRuleEvaluator::isEnabled)
                .sorted(Comparator.comparingInt(RiskRuleEvaluator::getPriority))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> triggerRiskAssessment(Long applicationId) {
        return performRiskAssessment(applicationId);
    }

    @Override
    @Transactional
    public Map<String, Object> batchRiskAssessment(List<Long> applicationIds) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> batchResults = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (Long applicationId : applicationIds) {
            try {
                Map<String, Object> assessmentResult = performRiskAssessment(applicationId);
                assessmentResult.put("applicationId", applicationId);
                batchResults.add(assessmentResult);
                successCount++;
            } catch (Exception e) {
                log.error("批量风险评估失败：applicationId={}, error={}", applicationId, e.getMessage(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("applicationId", applicationId);
                errorResult.put("success", false);
                errorResult.put("error", e.getMessage());
                batchResults.add(errorResult);
                failCount++;
            }
        }

        result.put("totalCount", applicationIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("results", batchResults);

        log.info("批量风险评估完成：total={}, success={}, fail={}", 
                applicationIds.size(), successCount, failCount);

        return result;
    }

    private void extractScoringCardDetails(List<Map<String, Object>> ruleResults, RiskReport riskReport) {
        for (Map<String, Object> ruleResult : ruleResults) {
            Object details = ruleResult.get("details");
            if (!(details instanceof Map<?, ?> detailsMap)) {
                continue;
            }
            Object totalPoints = detailsMap.get("totalPoints");
            Object maxPoints = detailsMap.get("maxPoints");
            Object cardVersion = detailsMap.get("cardVersion");
            if (totalPoints instanceof Number total) {
                riskReport.setScoringCardPoints(total.intValue());
            }
            if (maxPoints instanceof Number max) {
                riskReport.setScoringCardMax(max.intValue());
            }
            if (cardVersion != null) {
                riskReport.setCardVersion(String.valueOf(cardVersion));
            }
            break;
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("风控详情序列化失败：{}", e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> getRiskAssessmentDetail(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("贷款申请不存在：" + applicationId));

        RiskReport report = riskReportRepository.findByLoanApplication(application).orElse(null);
        if (report == null) {
            return performRiskAssessment(applicationId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", applicationId);
        result.put("overallRiskScore", report.getRiskScore());
        result.put("overallRiskLevel", report.getRiskLevel());
        result.put("passed", Boolean.TRUE.equals(report.getPassed()));
        result.put("rejectReason", report.getRejectReason());
        result.put("riskReportId", report.getId());
        result.put("scoringCardPoints", report.getScoringCardPoints());
        result.put("scoringCardMax", report.getScoringCardMax());
        result.put("cardVersion", report.getCardVersion());

        if (report.getDetailsJson() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = objectMapper.readValue(report.getDetailsJson(), Map.class);
                result.put("ruleResults", details.get("ruleResults"));
                result.put("featureSnapshot", details.get("featureSnapshot"));
                result.put("dataSourceResults", details.get("dataSourceResults"));
            } catch (JsonProcessingException e) {
                log.warn("解析风控详情失败：applicationId={}", applicationId);
            }
        }
        return result;
    }
}
