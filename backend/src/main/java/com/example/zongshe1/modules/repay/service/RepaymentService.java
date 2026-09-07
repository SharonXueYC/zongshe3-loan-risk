package com.example.zongshe1.service;

import com.example.zongshe1.dto.RepayPlanDTO;
import com.example.zongshe1.dto.RepaymentRecordDTO;
import com.example.zongshe1.dto.api.RepayRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface RepaymentService {

    // 获取用户的还款计划列表（用户端）
    List<RepayPlanDTO> getUserRepayPlans(String userId);

    // 获取某笔申请的还款计划
    List<RepayPlanDTO> getRepayPlansByApplicationId(Long applicationId);

    // 执行还款
    Map<String, Object> processRepayment(String userId, RepayRequest request);

    // 获取用户的还款记录
    List<RepaymentRecordDTO> getUserRepaymentRecords(String userId);

    // 获取某期计划的剩余应还金额
    BigDecimal getRemainingAmount(Long planId);

    // 管理端：获取所有还款记录
    List<RepaymentRecordDTO> getAllRepaymentRecords();

    // 管理端：根据申请ID获取还款记录
    List<RepaymentRecordDTO> getRepaymentRecordsByApplicationId(Long applicationId);

    // 管理端：获取全部还款计划（前端还款计划页）
    List<com.example.zongshe1.dto.AdminRepaymentViewDTO> getAllAdminRepaymentPlans();

    // 管理端：确认还款
    Map<String, Object> adminConfirmRepayment(Long planId);
}