package com.example.zongshe1.service.impl;

import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.Product;
import com.example.zongshe1.entity.RepaymentRecord;
import com.example.zongshe1.modules.repay.entity.RepayPlan;
import com.example.zongshe1.repository.LoanApplicationRepository;
import com.example.zongshe1.repository.ProductRepository;
import com.example.zongshe1.repository.RepayPlanRepository;
import com.example.zongshe1.repository.RepaymentRecordRepository;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RepayPlanRepository repayPlanRepository;
    private final RepaymentRecordRepository repaymentRecordRepository;

    @Override
    public Map<String, Object> getDashboardStatistics() {
        List<LoanApplication> allApplications = loanApplicationRepository.findAll();

        long totalApplications = allApplications.size();
        long approved = allApplications.stream().filter(a -> "approved".equals(a.getStatus())).count();
        long pending = allApplications.stream().filter(a -> "pending".equals(a.getStatus())).count();
        long rejected = allApplications.stream().filter(a -> "rejected".equals(a.getStatus())).count();

        BigDecimal totalAmount = allApplications.stream()
                .map(LoanApplication::getLoanAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("approved", approved);
        stats.put("pending", pending);
        stats.put("rejected", rejected);
        stats.put("total", totalApplications);
        stats.put("totalAmount", totalAmount);

        // 计算变化百分比（这里使用模拟数据）
        Random random = new Random();
        stats.put("approvedChange", random.nextInt(20) - 5); // -5到+15之间的随机数
        stats.put("pendingChange", random.nextInt(20) - 10); // -10到+10之间的随机数
        stats.put("rejectedChange", random.nextInt(15) - 3); // -3到+12之间的随机数
        stats.put("totalChange", random.nextInt(25) - 5); // -5到+20之间的随机数

        stats.put("totalUsers", userRepository.count());
        stats.put("todayNew", countTodayApplications(allApplications));
        stats.put("overdueRate", calculateOverdueRate());

        return stats;
    }

    private long countTodayApplications(List<LoanApplication> applications) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        return applications.stream()
                .filter(a -> a.getApplyTime() != null && !a.getApplyTime().isBefore(startOfDay))
                .count();
    }

    private double calculateOverdueRate() {
        List<RepayPlan> plans = repayPlanRepository.findAll();
        if (plans.isEmpty()) {
            return 0.0;
        }
        long overdue = plans.stream().filter(p -> "OVERDUE".equals(p.getStatus())).count();
        return BigDecimal.valueOf(overdue * 100.0 / plans.size())
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public Map<String, Object> getApplicationTrend() {
        List<LoanApplication> allApplications = loanApplicationRepository.findAll();

        // 按月份分组统计
        Map<String, Map<String, Long>> trendData = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = now.minusMonths(i);
            String monthKey = month.format(formatter);

            Map<String, Long> monthStats = new HashMap<>();
            monthStats.put("approved", 0L);
            monthStats.put("pending", 0L);
            monthStats.put("rejected", 0L);
            trendData.put(monthKey, monthStats);
        }

        // 填充实际数据（这里简化处理，实际应该按申请时间过滤）
        for (LoanApplication app : allApplications) {
            String monthKey = app.getApplyTime().format(formatter);
            if (trendData.containsKey(monthKey)) {
                Map<String, Long> monthStats = trendData.get(monthKey);
                monthStats.put(app.getStatus(), monthStats.get(app.getStatus()) + 1);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("trend", trendData);
        return result;
    }

    @Override
    public Map<String, Object> getProductSales() {
        List<LoanApplication> allApplications = loanApplicationRepository.findAll();
        List<Product> allProducts = productRepository.findAll();

        // 按产品类型统计
        Map<String, Long> salesByType = new HashMap<>();
        Map<String, String> productIdToName = allProducts.stream()
                .collect(Collectors.toMap(
                        Product::getProductNo,
                        Product::getProductName,
                        (p1, p2) -> p1
                ));

        // 初始化所有产品
        for (Product product : allProducts) {
            salesByType.put(product.getProductName(), 0L);
        }

        // 统计申请数量
        for (LoanApplication app : allApplications) {
            // 这里简化处理，实际应该根据贷款类型映射到产品
            if ("个人消费贷".equals(app.getLoanType())) {
                salesByType.put("个人消费贷", salesByType.get("个人消费贷") + 1);
            } else if ("房屋抵押贷款".equals(app.getLoanType())) {
                salesByType.put("房屋抵押贷款", salesByType.get("房屋抵押贷款") + 1);
            } else if ("汽车贷款".equals(app.getLoanType())) {
                salesByType.put("汽车贷款", salesByType.get("汽车贷款") + 1);
            } else if ("个人经营贷".equals(app.getLoanType())) {
                salesByType.put("个人经营贷", salesByType.get("个人经营贷") + 1);
            }
        }

        // 计算百分比
        long totalSales = salesByType.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Double> percentages = new HashMap<>();

        for (Map.Entry<String, Long> entry : salesByType.entrySet()) {
            double percentage = totalSales > 0 ? (double) entry.getValue() / totalSales * 100 : 0;
            percentages.put(entry.getKey(), percentage);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sales", salesByType);
        result.put("percentages", percentages);
        result.put("total", totalSales);

        return result;
    }

    @Override
    public Map<String, Object> getLoanStatusDistribution() {
        List<LoanApplication> allApplications = loanApplicationRepository.findAll();

        long total = allApplications.size();
        long approved = allApplications.stream().filter(a -> "approved".equals(a.getStatus())).count();
        long pending = allApplications.stream().filter(a -> "pending".equals(a.getStatus())).count();
        long rejected = allApplications.stream().filter(a -> "rejected".equals(a.getStatus())).count();

        double approvedPercent = total > 0 ? (double) approved / total * 100 : 0;
        double pendingPercent = total > 0 ? (double) pending / total * 100 : 0;
        double rejectedPercent = total > 0 ? (double) rejected / total * 100 : 0;

        Map<String, Long> counts = new HashMap<>();
        counts.put("approved", approved);
        counts.put("pending", pending);
        counts.put("rejected", rejected);

        Map<String, Double> percentages = new HashMap<>();
        percentages.put("approved", approvedPercent);
        percentages.put("pending", pendingPercent);
        percentages.put("rejected", rejectedPercent);

        Map<String, Object> result = new HashMap<>();
        result.put("counts", counts);
        result.put("percentages", percentages);
        result.put("total", total);

        return result;
    }

    @Override
    public Map<String, Object> getChartData() {
        List<LoanApplication> allApplications = loanApplicationRepository.findAll();
        List<RepayPlan> allPlans = repayPlanRepository.findAll();
        List<RepaymentRecord> allRecords = repaymentRecordRepository.findAll();

        int[] lineChartData = new int[12];
        double[] barChartData = new double[12];

        for (LoanApplication app : allApplications) {
            if (app.getApplyTime() == null) {
                continue;
            }
            int monthIndex = app.getApplyTime().getMonthValue() - 1;
            lineChartData[monthIndex]++;
        }

        // 月度还款：按实际还款时间统计；仅计入当年且不超过当前月（排除未来月份）
        YearMonth currentMonth = YearMonth.now();
        Set<Long> plansWithRecords = new HashSet<>();
        for (RepaymentRecord record : allRecords) {
            if (record.getRepayTime() == null || record.getAmount() == null) {
                continue;
            }
            YearMonth repayMonth = YearMonth.from(record.getRepayTime());
            if (!isCountableRepayMonth(repayMonth, currentMonth)) {
                continue;
            }
            if (record.getRepayPlan() != null && record.getRepayPlan().getId() != null) {
                plansWithRecords.add(record.getRepayPlan().getId());
            }
            barChartData[repayMonth.getMonthValue() - 1] += record.getAmount().doubleValue();
        }

        // 无还款流水的演示数据：按应还月兜底，同样排除未来月份
        for (RepayPlan plan : allPlans) {
            if (!"PAID".equalsIgnoreCase(plan.getStatus())) {
                continue;
            }
            if (plan.getId() != null && plansWithRecords.contains(plan.getId())) {
                continue;
            }
            if (plan.getDueDate() == null) {
                continue;
            }
            YearMonth dueMonth = YearMonth.from(plan.getDueDate());
            if (!isCountableRepayMonth(dueMonth, currentMonth)) {
                continue;
            }
            BigDecimal paid = plan.getPaidTotal() != null && plan.getPaidTotal().signum() > 0
                    ? plan.getPaidTotal()
                    : plan.getTotalAmount();
            if (paid != null) {
                barChartData[dueMonth.getMonthValue() - 1] += paid.doubleValue();
            }
        }

        long approved = allApplications.stream().filter(a -> "approved".equalsIgnoreCase(a.getStatus())).count();
        long pending = allApplications.stream().filter(a -> "pending".equalsIgnoreCase(a.getStatus())).count();
        long rejected = allApplications.stream().filter(a -> "rejected".equalsIgnoreCase(a.getStatus())).count();
        long paid = allApplications.stream()
                .filter(a -> "settled".equalsIgnoreCase(a.getStatus()) || "paid".equalsIgnoreCase(a.getStatus()))
                .count();

        List<Map<String, Object>> pieChartData = List.of(
                Map.of("value", approved, "name", "已批准"),
                Map.of("value", pending, "name", "审核中"),
                Map.of("value", rejected, "name", "已拒绝"),
                Map.of("value", paid, "name", "已还清")
        );

        var allUsers = userRepository.findAll();
        long vipUsers = allUsers.stream().filter(u -> u.getCreditScore() != null && u.getCreditScore() >= 700).count();
        long normalUsers = allUsers.stream()
                .filter(u -> u.getCreditScore() != null && u.getCreditScore() >= 600 && u.getCreditScore() < 700)
                .count();
        long newUsers = allUsers.stream().filter(u -> u.getCreditScore() == null || u.getCreditScore() < 600).count();

        List<Map<String, Object>> pieChart2Data = List.of(
                Map.of("value", vipUsers, "name", "VIP用户"),
                Map.of("value", normalUsers, "name", "普通用户"),
                Map.of("value", newUsers, "name", "新用户")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("lineChartData", lineChartData);
        result.put("barChartData", barChartData);
        result.put("barChartMonthCount", currentMonth.getMonthValue());
        result.put("pieChartData", pieChartData);
        result.put("pieChart2Data", pieChart2Data);
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentLoans(int limit) {
        return loanApplicationRepository.findAll().stream()
                .sorted(Comparator.comparing(LoanApplication::getApplyTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .map(app -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", app.getId());
                    item.put("applicant", app.getApplicantName());
                    item.put("amount", app.getLoanAmount());
                    item.put("term", app.getLoanTerm());
                    item.put("status", normalizeLoanStatus(app.getStatus()));
                    item.put("statusText", loanStatusText(app.getStatus()));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private boolean isCountableRepayMonth(YearMonth repayMonth, YearMonth currentMonth) {
        return !repayMonth.isAfter(currentMonth) && repayMonth.getYear() == currentMonth.getYear();
    }

    private String normalizeLoanStatus(String status) {
        if (status == null) {
            return "pending";
        }
        String lower = status.toLowerCase(Locale.ROOT);
        if ("approved".equals(lower)) {
            return "approved";
        }
        if ("disbursed".equals(lower)) {
            return "disbursed";
        }
        if ("rejected".equals(lower)) {
            return "rejected";
        }
        if ("settled".equals(lower) || "paid".equals(lower)) {
            return "paid";
        }
        return "pending";
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
}