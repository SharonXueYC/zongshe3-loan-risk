package com.example.zongshe1.modules.repay.schedule;

import com.example.zongshe1.modules.repay.entity.RepayPlan;
import com.example.zongshe1.repository.RepayPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RepaymentOverdueScheduler {

    private final RepayPlanRepository repayPlanRepository;

    @Value("${app.system.overdue-penalty-rate:0.0005}")
    private double overduePenaltyRate;

    @Scheduled(cron = "${repay.overdue.cron:0 0 1 * * ?}")
    @Transactional
    public void markOverduePlans() {
        LocalDate today = LocalDate.now();
        List<RepayPlan> pending = repayPlanRepository.findByStatusIn(List.of("PENDING", "OVERDUE"));
        int updated = 0;
        for (RepayPlan plan : pending) {
            if (plan.getDueDate() == null || !plan.getDueDate().isBefore(today)) {
                continue;
            }
            if ("PENDING".equals(plan.getStatus())) {
                plan.setStatus("OVERDUE");
                applyPenalty(plan);
                updated++;
            }
        }
        if (updated > 0) {
            repayPlanRepository.saveAll(pending);
            log.info("逾期检测：标记 {} 条还款计划为 OVERDUE", updated);
        }
    }

    private void applyPenalty(RepayPlan plan) {
        BigDecimal base = plan.getTotalAmount() != null ? plan.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal penalty = base.multiply(BigDecimal.valueOf(overduePenaltyRate))
                .setScale(2, RoundingMode.HALF_UP);
        plan.setInterest(plan.getInterest().add(penalty));
        plan.setTotalAmount(plan.getPrincipal().add(plan.getInterest()).setScale(2, RoundingMode.HALF_UP));
    }
}
