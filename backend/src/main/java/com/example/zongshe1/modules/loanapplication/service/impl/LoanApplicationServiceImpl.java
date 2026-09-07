package com.example.zongshe1.service.impl;

import com.example.zongshe1.dto.LoanApplicationDTO;
import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.modules.repay.entity.RepayPlan;
import com.example.zongshe1.repository.LoanApplicationRepository;
import com.example.zongshe1.repository.RepayPlanRepository;
import com.example.zongshe1.repository.RiskReportRepository;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.service.ContractService;
import com.example.zongshe1.service.CreditScoreCalculator;
import com.example.zongshe1.service.LoanApplicationService;
import com.example.zongshe1.service.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final RepayPlanRepository repayPlanRepository;
    private final RiskService riskService;
    private final RiskReportRepository riskReportRepository;
    private final List<CreditScoreCalculator> creditScoreCalculators;
    private final ContractService contractService;

    @Override
    public List<LoanApplicationDTO> getApplications(String status, String loanType, String search) {
        List<LoanApplication> applications;

        if (status != null && !status.equals("all") && !status.isEmpty()) {
            applications = loanApplicationRepository.findByStatus(status);
        } else if (loanType != null && !loanType.equals("all") && !loanType.isEmpty()) {
            applications = loanApplicationRepository.findByLoanType(loanType);
        } else {
            applications = loanApplicationRepository.findAll();
        }

        // 应用搜索过滤
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.toLowerCase();
            applications = applications.stream()
                    .filter(app -> app.getApplicantName().toLowerCase().contains(searchTerm) ||
                            app.getApplicationNo().toLowerCase().contains(searchTerm) ||
                            app.getPhoneNumber().contains(searchTerm))
                    .collect(Collectors.toList());
        }

        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LoanApplicationDTO getApplicationById(Long id) {
        return loanApplicationRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean approveApplication(Long id, String remark) {
        return loanApplicationRepository.findById(id)
                .map(application -> {
                    application.setStatus("approved");
                    application.setAuditRemark(remark);
                    application.setAuditTime(LocalDateTime.now());
                    loanApplicationRepository.save(application);
                    updateCreditByEvent(application, "APPROVE");
                    try {
                        contractService.createContractForApplication(application.getId());
                    } catch (Exception ex) {
                        log.warn("审批通过后自动生成合同失败：applicationId={}", id, ex);
                    }
                    ensureRepayPlansForApplication(application);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean rejectApplication(Long id, String remark) {
        return loanApplicationRepository.findById(id)
                .map(application -> {
                    application.setStatus("rejected");
                    application.setAuditRemark(remark);
                    application.setAuditTime(LocalDateTime.now());
                    loanApplicationRepository.save(application);
                    updateCreditByEvent(application, "REJECT");
                    return true;
                })
                .orElse(false);
    }

    @Override
    public Map<String, Object> getStatistics() {
        List<LoanApplication> allApplications = loanApplicationRepository.findAll();

        Map<String, Object> statistics = new HashMap<>();

        // 统计申请数量
        long total = allApplications.size();
        long approved = allApplications.stream().filter(a -> "approved".equals(a.getStatus())).count();
        long pending = allApplications.stream().filter(a -> "pending".equals(a.getStatus())).count();
        long rejected = allApplications.stream().filter(a -> "rejected".equals(a.getStatus())).count();

        // 统计申请金额
        BigDecimal totalAmount = allApplications.stream()
                .map(LoanApplication::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算百分比
        double approvedPercent = total > 0 ? (double) approved / total * 100 : 0;
        double pendingPercent = total > 0 ? (double) pending / total * 100 : 0;
        double rejectedPercent = total > 0 ? (double) rejected / total * 100 : 0;

        statistics.put("total", total);
        statistics.put("approved", approved);
        statistics.put("pending", pending);
        statistics.put("rejected", rejected);
        statistics.put("totalAmount", totalAmount);
        statistics.put("approvedPercent", Math.round(approvedPercent));
        statistics.put("pendingPercent", Math.round(pendingPercent));
        statistics.put("rejectedPercent", Math.round(rejectedPercent));

        // 按贷款类型统计
        Map<String, Long> byType = allApplications.stream()
                .collect(Collectors.groupingBy(LoanApplication::getLoanType, Collectors.counting()));
        statistics.put("byType", byType);

        // 最近6个月的申请趋势
        Map<String, Map<String, Long>> trendData = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            String monthKey = month.getYear() + "-" + String.format("%02d", month.getMonthValue());

            Map<String, Long> monthStats = new HashMap<>();
            monthStats.put("approved", 0L);
            monthStats.put("pending", 0L);
            monthStats.put("rejected", 0L);

            // 这里简化处理，实际应该按月份查询
            trendData.put(monthKey, monthStats);
        }
        statistics.put("trend", trendData);

        return statistics;
    }

    @Override
    @Transactional
    public Map<String, Object> submitApplication(String userId, BigDecimal loanAmount, 
                                                Integer loanTerm, String loanType, 
                                                String repaymentMode, BigDecimal interestRate, 
                                                String description) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 查找用户
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 2. 检查用户状态
            if (user.getUserStatus() != 1) {
                result.put("success", false);
                result.put("message", "用户账户状态异常，无法申请贷款");
                return result;
            }

            // 3. 检查贷款资格
            if (user.getCreditScore() < 450) {
                result.put("success", false);
                result.put("message", "信用分不足，无法申请贷款");
                return result;
            }

            // 4. 生成申请单号
            String applicationNo = "LA-" + System.currentTimeMillis();

            // 5. 创建贷款申请
            LoanApplication application = new LoanApplication();
            application.setApplicationNo(applicationNo);
            application.setApplicantName(user.getUserName() != null ? user.getUserName() : "未设置");
            application.setIdCardNumber(user.getIdCardNumber() != null ? user.getIdCardNumber() : "");
            application.setPhoneNumber(user.getPhoneNumber());
            application.setLoanType(loanType != null ? loanType : "个人消费贷");
            application.setLoanAmount(loanAmount);
            application.setLoanTerm(loanTerm);
            application.setApplyTime(LocalDateTime.now());
            application.setStatus("pending");
            application.setDescription(description);
            application.setUser(user);

            // 6. 保存申请
            loanApplicationRepository.save(application);
            updateCreditByEvent(application, "SUBMIT");

            // 7. 触发风控评估（评分卡 + 规则链）
            Map<String, Object> riskAssessment = null;
            try {
                riskAssessment = riskService.performRiskAssessment(application.getId());
            } catch (Exception riskEx) {
                log.error("贷款申请风控评估失败：applicationId={}", application.getId(), riskEx);
            }

            // 8. 返回结果
            result.put("success", true);
            result.put("applicationNo", applicationNo);
            result.put("applicationId", application.getId());
            result.put("message", "贷款申请提交成功");
            if (riskAssessment != null) {
                result.put("riskAssessment", riskAssessment);
                result.put("riskPassed", riskAssessment.get("passed"));
            }

            log.info("贷款申请提交成功：userId={}, applicationNo={}", userId, applicationNo);

        } catch (Exception e) {
            log.error("提交贷款申请失败", e);
            result.put("success", false);
            result.put("message", "提交申请失败：" + e.getMessage());
        }

        return result;
    }

    private void ensureRepayPlansForApplication(LoanApplication application) {
        List<RepayPlan> existing = repayPlanRepository.findByLoanApplication_IdOrderByPeriodNoAsc(application.getId());
        if (!existing.isEmpty()) {
            return;
        }
        createRepayPlans(application, "equal", BigDecimal.valueOf(12));
    }

    private void createRepayPlans(LoanApplication application, String repaymentMode, BigDecimal interestRate) {
        int term = Math.max(1, application.getLoanTerm() == null ? 1 : application.getLoanTerm());
        BigDecimal annualRate = interestRate == null ? BigDecimal.valueOf(12) : interestRate;
        BigDecimal monthlyRate = annualRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        BigDecimal principalPerPeriod = application.getLoanAmount()
                .divide(BigDecimal.valueOf(term), 2, RoundingMode.HALF_UP);
        BigDecimal equalPayment = calculateEqualPayment(application.getLoanAmount(), monthlyRate, term);

        for (int i = 1; i <= term; i++) {
            RepayPlan plan = new RepayPlan();
            plan.setPlanNo("PLAN-" + application.getApplicationNo() + "-" + i);
            plan.setLoanApplication(application);
            plan.setPeriodNo(i);
            plan.setDueDate(LocalDate.now().plusMonths(i));

            BigDecimal principal;
            BigDecimal interest;
            if ("principal".equalsIgnoreCase(repaymentMode)) {
                principal = principalPerPeriod;
                BigDecimal remainPrincipal = application.getLoanAmount()
                        .subtract(principalPerPeriod.multiply(BigDecimal.valueOf(i - 1)));
                interest = remainPrincipal.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            } else {
                // 默认按等额本息处理，便于前端月供展示与待还计划一致
                interest = application.getLoanAmount().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                principal = equalPayment.subtract(interest).setScale(2, RoundingMode.HALF_UP);
            }

            plan.setPrincipal(principal);
            plan.setInterest(interest);
            plan.setTotalAmount(principal.add(interest).setScale(2, RoundingMode.HALF_UP));
            plan.setPaidPrincipal(BigDecimal.ZERO);
            plan.setPaidInterest(BigDecimal.ZERO);
            plan.setPaidTotal(BigDecimal.ZERO);
            plan.setStatus("PENDING");
            repayPlanRepository.save(plan);
        }
    }

    private BigDecimal calculateEqualPayment(BigDecimal amount, BigDecimal monthlyRate, int term) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            return amount.divide(BigDecimal.valueOf(term), 2, RoundingMode.HALF_UP);
        }
        double r = monthlyRate.doubleValue();
        double n = term;
        double factor = Math.pow(1 + r, n);
        double payment = amount.doubleValue() * r * factor / (factor - 1);
        return BigDecimal.valueOf(payment).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<LoanApplicationDTO> getApplicationsByUserId(String userId) {
        List<LoanApplication> applications = loanApplicationRepository.findByUserIdOrderByApplyTimeDesc(userId);
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private LoanApplicationDTO convertToDTO(LoanApplication application) {
        LoanApplicationDTO dto = new LoanApplicationDTO();
        dto.setId(application.getId());
        dto.setApplicationNo(application.getApplicationNo());
        dto.setApplicantName(application.getApplicantName());
        dto.setIdCardNumber(application.getIdCardNumber());
        dto.setPhoneNumber(application.getPhoneNumber());
        dto.setLoanType(application.getLoanType());
        dto.setLoanAmount(application.getLoanAmount());
        dto.setLoanTerm(application.getLoanTerm());
        dto.setApplyTime(application.getApplyTime());
        dto.setStatus(application.getStatus());
        dto.setStatusText(loanStatusText(application.getStatus()));
        dto.setAuditRemark(application.getAuditRemark());
        dto.setAuditTime(application.getAuditTime());
        dto.setIncomeInfo(application.getIncomeInfo());
        dto.setDescription(application.getDescription());

        riskReportRepository.findByLoanApplication_Id(application.getId()).ifPresent(report -> {
            dto.setRiskScore(report.getRiskScore());
            dto.setRiskLevel(report.getRiskLevel());
            dto.setRiskPassed(report.getPassed());
            dto.setScoringCardPoints(report.getScoringCardPoints());
            dto.setScoringCardMax(report.getScoringCardMax());
            dto.setRejectReason(report.getRejectReason());
        });
        return dto;
    }

    private String loanStatusText(String status) {
        if (status == null || status.isBlank()) {
            return "未知";
        }
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "pending" -> "审核中";
            case "approved" -> "已通过";
            case "rejected" -> "已拒绝";
            case "disbursed" -> "已放款";
            case "settled", "paid" -> "已还清";
            default -> status;
        };
    }

    private void updateCreditByEvent(LoanApplication application, String eventType) {
        if (application.getUser() == null) {
            return;
        }
        creditScoreCalculators.stream()
                .min((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .ifPresent(calculator -> calculator.updateCreditScoreByLoanEvent(
                        application.getUser().getUserId(), application, eventType));
    }
}