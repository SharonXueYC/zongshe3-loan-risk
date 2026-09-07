package com.example.zongshe1.service.impl;

import com.example.zongshe1.dto.AdminRepaymentViewDTO;
import com.example.zongshe1.dto.RepayPlanDTO;
import com.example.zongshe1.dto.RepaymentRecordDTO;
import com.example.zongshe1.dto.api.RepayRequest;
import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.RepaymentRecord;
import com.example.zongshe1.exception.BusinessException;
import com.example.zongshe1.modules.repay.entity.RepayPlan;
import com.example.zongshe1.repository.LoanApplicationRepository;
import com.example.zongshe1.repository.RepayPlanRepository;
import com.example.zongshe1.repository.RepaymentRecordRepository;
import com.example.zongshe1.service.CreditScoreCalculator;
import com.example.zongshe1.service.RepaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RepaymentServiceImpl implements RepaymentService {

    private final RepayPlanRepository repayPlanRepository;
    private final RepaymentRecordRepository repaymentRecordRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final List<CreditScoreCalculator> creditScoreCalculators;

    @Override
    public List<RepayPlanDTO> getUserRepayPlans(String userId) {
        List<RepayPlan> plans = repayPlanRepository.findPendingPlansByUserId(userId);
        return plans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<RepayPlanDTO> getRepayPlansByApplicationId(Long applicationId) {
        List<RepayPlan> plans = repayPlanRepository.findByLoanApplication_IdOrderByPeriodNoAsc(applicationId);
        return plans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> processRepayment(String userId, RepayRequest request) {
        // 1. 查询还款计划
        RepayPlan plan = repayPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new BusinessException("还款计划不存在"));

        // 2. 权限校验：确保还款计划属于该用户
        if (!plan.getLoanApplication().getUser().getUserId().equals(userId)) {
            throw new BusinessException("无权操作该还款计划");
        }

        // 3. 校验计划状态
        if (!"PENDING".equals(plan.getStatus()) && !"OVERDUE".equals(plan.getStatus())) {
            throw new BusinessException("该期还款计划已结清或状态异常");
        }

        // 4. 计算剩余应还金额
        BigDecimal remaining = getRemainingAmount(plan);
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new BusinessException("还款金额不能超过剩余应还金额");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("还款金额必须大于0");
        }

        // 5. 更新还款计划
        BigDecimal newPaidTotal = plan.getPaidTotal() == null ?
                request.getAmount() : plan.getPaidTotal().add(request.getAmount());
        plan.setPaidTotal(newPaidTotal);

        // 按比例分配本金和利息（简化处理：优先还利息，再还本金）
        BigDecimal remainingInterest = plan.getInterest().subtract(
                plan.getPaidInterest() == null ? BigDecimal.ZERO : plan.getPaidInterest());
        BigDecimal paidInterest;
        BigDecimal paidPrincipal;

        if (request.getAmount().compareTo(remainingInterest) <= 0) {
            paidInterest = request.getAmount();
            paidPrincipal = BigDecimal.ZERO;
        } else {
            paidInterest = remainingInterest;
            paidPrincipal = request.getAmount().subtract(remainingInterest);
        }

        plan.setPaidInterest(
                (plan.getPaidInterest() == null ? BigDecimal.ZERO : plan.getPaidInterest()).add(paidInterest)
        );
        plan.setPaidPrincipal(
                (plan.getPaidPrincipal() == null ? BigDecimal.ZERO : plan.getPaidPrincipal()).add(paidPrincipal)
        );

        // 判断是否已全额还清
        boolean justPaid = false;
        if (plan.getPaidTotal().compareTo(plan.getTotalAmount()) >= 0) {
            justPaid = !"PAID".equals(plan.getStatus());
            plan.setStatus("PAID");
        }
        plan.setUpdatedAt(LocalDate.now());
        repayPlanRepository.save(plan);

        // 6. 创建还款记录
        RepaymentRecord record = new RepaymentRecord();
        record.setRepayNo(generateRepayNo());
        record.setRepayPlan(plan);
        record.setLoanApplication(plan.getLoanApplication());
        record.setAmount(request.getAmount());
        record.setRepayTime(LocalDateTime.now());
        record.setRepayMethod(request.getRepayMethod() != null ? request.getRepayMethod() : "ONLINE");
        record.setTransactionId(request.getTransactionId());
        record.setRemark("用户还款");
        repaymentRecordRepository.save(record);

        // 7. 如果该申请所有期数都已还清，可以更新贷款申请状态为“已结清”
        checkAndUpdateApplicationStatus(plan.getLoanApplication());

        if (justPaid) {
            creditScoreCalculators.stream()
                    .min((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                    .ifPresent(calculator -> calculator.updateCreditScoreByLoanEvent(
                            userId, plan.getLoanApplication(), "REPAY"));
        }

        log.info("用户 {} 成功还款 {} 元，计划ID：{}", userId, request.getAmount(), request.getPlanId());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "还款成功");
        result.put("repayNo", record.getRepayNo());
        return result;
    }

    @Override
    public BigDecimal getRemainingAmount(Long planId) {
        RepayPlan plan = repayPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("还款计划不存在"));
        return getRemainingAmount(plan);
    }

    private BigDecimal getRemainingAmount(RepayPlan plan) {
        BigDecimal paid = plan.getPaidTotal() == null ? BigDecimal.ZERO : plan.getPaidTotal();
        return plan.getTotalAmount().subtract(paid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentRecordDTO> getUserRepaymentRecords(String userId) {
        List<RepaymentRecord> records =
                repaymentRecordRepository.findByLoanApplication_User_UserIdOrderByRepayTimeDesc(userId);
        return records.stream().map(this::convertRecordToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentRecordDTO> getAllRepaymentRecords() {
        List<RepaymentRecord> records = repaymentRecordRepository.findAll();
        return records.stream()
                .sorted(Comparator.comparing(RepaymentRecord::getRepayTime).reversed())
                .map(this::convertRecordToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepaymentRecordDTO> getRepaymentRecordsByApplicationId(Long applicationId) {
        List<RepaymentRecord> records = repaymentRecordRepository.findByLoanApplication_IdOrderByRepayTimeDesc(applicationId);
        return records.stream().map(this::convertRecordToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRepaymentViewDTO> getAllAdminRepaymentPlans() {
        List<RepayPlan> plans = repayPlanRepository.findAll();
        Map<Long, Integer> totalPeriodsMap = new HashMap<>();
        for (RepayPlan plan : plans) {
            Long appId = plan.getLoanApplication().getId();
            totalPeriodsMap.merge(appId, 1, Integer::sum);
        }

        return plans.stream()
                .sorted(Comparator.comparing(RepayPlan::getDueDate))
                .map(plan -> toAdminView(plan, totalPeriodsMap.getOrDefault(plan.getLoanApplication().getId(), 1)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> adminConfirmRepayment(Long planId) {
        RepayPlan plan = repayPlanRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("还款计划不存在"));

        if ("PAID".equals(plan.getStatus())) {
            throw new BusinessException("该期已还款");
        }

        plan.setPaidPrincipal(plan.getPrincipal());
        plan.setPaidInterest(plan.getInterest());
        plan.setPaidTotal(plan.getTotalAmount());
        plan.setStatus("PAID");
        plan.setUpdatedAt(LocalDate.now());
        repayPlanRepository.save(plan);

        RepaymentRecord record = new RepaymentRecord();
        record.setRepayNo(generateRepayNo());
        record.setRepayPlan(plan);
        record.setLoanApplication(plan.getLoanApplication());
        record.setAmount(plan.getTotalAmount());
        record.setRepayTime(LocalDateTime.now());
        record.setRepayMethod("ADMIN");
        record.setRemark("管理员确认还款");
        repaymentRecordRepository.save(record);

        checkAndUpdateApplicationStatus(plan.getLoanApplication());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "还款确认成功");
        return result;
    }

    private AdminRepaymentViewDTO toAdminView(RepayPlan plan, int totalPeriods) {
        AdminRepaymentViewDTO dto = new AdminRepaymentViewDTO();
        dto.setId(plan.getId());
        dto.setLoanId(plan.getLoanApplication().getId());
        dto.setApplicant(plan.getLoanApplication().getApplicantName());
        dto.setPeriod(plan.getPeriodNo());
        dto.setTotalPeriods(totalPeriods);
        dto.setAmount(plan.getTotalAmount());
        dto.setPrincipal(plan.getPrincipal());
        dto.setInterest(plan.getInterest());
        dto.setDueDate(plan.getDueDate() != null ? plan.getDueDate().toString() : "");
        if ("PAID".equals(plan.getStatus())) {
            String paidDate = repaymentRecordRepository.findByRepayPlan_Id(plan.getId()).stream()
                    .map(RepaymentRecord::getRepayTime)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .map(t -> t.toLocalDate().toString())
                    .orElse(plan.getUpdatedAt() != null ? plan.getUpdatedAt().toString() : LocalDate.now().toString());
            dto.setPaidDate(paidDate);
        }
        mapRepaymentStatus(dto, plan.getStatus());
        return dto;
    }

    private void mapRepaymentStatus(AdminRepaymentViewDTO dto, String status) {
        if (status == null) {
            dto.setStatus("pending");
            dto.setStatusText("待还款");
            return;
        }
        switch (status.toUpperCase(Locale.ROOT)) {
            case "PAID" -> {
                dto.setStatus("paid");
                dto.setStatusText("已还款");
            }
            case "OVERDUE" -> {
                dto.setStatus("overdue");
                dto.setStatusText("逾期");
            }
            default -> {
                dto.setStatus("pending");
                dto.setStatusText("待还款");
            }
        }
    }

    // ---------- 辅助方法 ----------
    private RepayPlanDTO convertToDTO(RepayPlan plan) {
        RepayPlanDTO dto = new RepayPlanDTO();
        dto.setId(plan.getId());
        dto.setPlanNo(plan.getPlanNo());
        dto.setPeriodNo(plan.getPeriodNo());
        dto.setDueDate(plan.getDueDate());
        dto.setPrincipal(plan.getPrincipal());
        dto.setInterest(plan.getInterest());
        dto.setTotalAmount(plan.getTotalAmount());
        dto.setPaidPrincipal(plan.getPaidPrincipal() != null ? plan.getPaidPrincipal() : BigDecimal.ZERO);
        dto.setPaidInterest(plan.getPaidInterest() != null ? plan.getPaidInterest() : BigDecimal.ZERO);
        dto.setPaidTotal(plan.getPaidTotal() != null ? plan.getPaidTotal() : BigDecimal.ZERO);
        dto.setRemainingAmount(getRemainingAmount(plan));
        dto.setStatus(plan.getStatus());
        dto.setApplicationNo(plan.getLoanApplication().getApplicationNo());
        return dto;
    }

    private RepaymentRecordDTO convertRecordToDTO(RepaymentRecord record) {
        RepaymentRecordDTO dto = new RepaymentRecordDTO();
        dto.setRepayNo(record.getRepayNo());
        dto.setPlanNo(record.getRepayPlan().getPlanNo());
        dto.setPeriodNo(record.getRepayPlan().getPeriodNo());
        LoanApplication app = record.getLoanApplication();
        dto.setApplicationNo(app.getApplicationNo());
        LocalDateTime loanApplyTime = app.getApplyTime() != null ? app.getApplyTime() : app.getCreatedAt();
        dto.setLoanApplyTime(loanApplyTime);
        dto.setAmount(record.getAmount());
        dto.setRepayTime(record.getRepayTime());
        dto.setRepayMethod(record.getRepayMethod());
        dto.setTransactionId(record.getTransactionId());
        return dto;
    }

    private String generateRepayNo() {
        return "REPAY-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private void checkAndUpdateApplicationStatus(LoanApplication application) {
        List<RepayPlan> plans = repayPlanRepository.findByLoanApplication_IdOrderByPeriodNoAsc(application.getId());
        boolean allPaid = plans.stream().allMatch(p -> "PAID".equals(p.getStatus()));
        if (allPaid && !"SETTLED".equals(application.getStatus())) {
            application.setStatus("SETTLED"); // 假设有 SETTLED 状态
            loanApplicationRepository.save(application);
        }
    }
}