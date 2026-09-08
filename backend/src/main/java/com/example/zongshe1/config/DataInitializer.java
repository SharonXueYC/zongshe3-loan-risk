package com.example.zongshe1.config;

import com.example.zongshe1.entity.Admin;
import com.example.zongshe1.entity.LoanApplication;
import com.example.zongshe1.entity.Product;
import com.example.zongshe1.entity.User;
import com.example.zongshe1.modules.crawler.service.DataCrawlerService;
import com.example.zongshe1.modules.repay.entity.RepayPlan;
import com.example.zongshe1.repository.AdminRepository;
import com.example.zongshe1.repository.LoanApplicationRepository;
import com.example.zongshe1.repository.ProductRepository;
import com.example.zongshe1.repository.RepayPlanRepository;
import com.example.zongshe1.repository.RiskReportRepository;
import com.example.zongshe1.repository.UserRepository;
import com.example.zongshe1.service.RiskService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final AdminRepository adminRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final RepayPlanRepository repayPlanRepository;
    private final RiskReportRepository riskReportRepository;
    private final RiskService riskService;
    private final DataCrawlerService dataCrawlerService;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        initAdmins();
        initProducts();
        initDemoUsers();
        initHistoricalDemoData();
        initHistoricalRepayPlans();
        initDemoRepayPlan();
        backfillRiskReports();
        log.info("数据初始化完成");
    }

    private void initAdmins() {
        if (adminRepository.count() == 0) {
            // 创建超级管理员
            Admin superAdmin = new Admin();
            superAdmin.setUsername("yunizai");
            superAdmin.setPassword(passwordEncoder.encode("yunizai123"));
            superAdmin.setName("芋泥崽");
            superAdmin.setRole("super_admin");
            superAdmin.setEmail("admin@yunizai.com");
            superAdmin.setPhoneNumber("13800138000");
            superAdmin.setIsActive(true);
            superAdmin.setCreatedAt(LocalDateTime.now());
            superAdmin.setUpdatedAt(LocalDateTime.now());
            adminRepository.save(superAdmin);

            // 创建普通管理员
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("普通管理员");
            admin.setRole("admin");
            admin.setEmail("admin@example.com");
            admin.setPhoneNumber("13900139000");
            admin.setIsActive(true);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            adminRepository.save(admin);

            // 创建财务管理员
            Admin finance = new Admin();
            finance.setUsername("finance");
            finance.setPassword(passwordEncoder.encode("finance123"));
            finance.setName("财务管理员");
            finance.setRole("finance");
            finance.setEmail("finance@example.com");
            finance.setPhoneNumber("13700137000");
            finance.setIsActive(true);
            finance.setCreatedAt(LocalDateTime.now());
            finance.setUpdatedAt(LocalDateTime.now());
            adminRepository.save(finance);

            // 创建操作员
            Admin operator = new Admin();
            operator.setUsername("operator");
            operator.setPassword(passwordEncoder.encode("operator123"));
            operator.setName("操作员");
            operator.setRole("operator");
            operator.setEmail("operator@example.com");
            operator.setPhoneNumber("13600136000");
            operator.setIsActive(true);
            operator.setCreatedAt(LocalDateTime.now());
            operator.setUpdatedAt(LocalDateTime.now());
            adminRepository.save(operator);

            log.info("管理员数据初始化完成");
        }
    }

    private void initProducts() {
        if (productRepository.count() == 0) {
            // 个人消费贷
            Product product1 = new Product();
            product1.setProductNo("PROD-2024-001");
            product1.setProductName("个人消费贷");
            product1.setProductType("个人消费贷款");
            product1.setMinAmount(BigDecimal.valueOf(1000));
            product1.setMaxAmount(BigDecimal.valueOf(50000));
            product1.setMinTerm(3);
            product1.setMaxTerm(36);
            product1.setInterestRate(BigDecimal.valueOf(8.5));
            product1.setProductDescription("灵活的个人消费贷款，用于日常消费支出");
            product1.setStatus("active");
            product1.setCreatedAt(LocalDateTime.now());
            product1.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product1);

            // 个人经营贷
            Product product2 = new Product();
            product2.setProductNo("PROD-2024-002");
            product2.setProductName("个人经营贷");
            product2.setProductType("个人经营贷款");
            product2.setMinAmount(BigDecimal.valueOf(50000));
            product2.setMaxAmount(BigDecimal.valueOf(500000));
            product2.setMinTerm(6);
            product2.setMaxTerm(60);
            product2.setInterestRate(BigDecimal.valueOf(7.8));
            product2.setProductDescription("为个体工商户和小微企业提供的经营资金贷款");
            product2.setStatus("active");
            product2.setCreatedAt(LocalDateTime.now());
            product2.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product2);

            // 房屋抵押贷款
            Product product3 = new Product();
            product3.setProductNo("PROD-2024-003");
            product3.setProductName("房屋抵押贷款");
            product3.setProductType("房屋抵押贷款");
            product3.setMinAmount(BigDecimal.valueOf(100000));
            product3.setMaxAmount(BigDecimal.valueOf(2000000));
            product3.setMinTerm(12);
            product3.setMaxTerm(120);
            product3.setInterestRate(BigDecimal.valueOf(6.2));
            product3.setProductDescription("以房屋作为抵押物的大额贷款，用于购房、装修等");
            product3.setStatus("active");
            product3.setCreatedAt(LocalDateTime.now());
            product3.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product3);

            // 汽车贷款
            Product product4 = new Product();
            product4.setProductNo("PROD-2024-004");
            product4.setProductName("汽车贷款");
            product4.setProductType("汽车贷款");
            product4.setMinAmount(BigDecimal.valueOf(30000));
            product4.setMaxAmount(BigDecimal.valueOf(300000));
            product4.setMinTerm(12);
            product4.setMaxTerm(48);
            product4.setInterestRate(BigDecimal.valueOf(7.2));
            product4.setProductDescription("用于购买新车或二手车的专项贷款");
            product4.setStatus("inactive");
            product4.setCreatedAt(LocalDateTime.now());
            product4.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product4);

            // 小额应急贷
            Product product5 = new Product();
            product5.setProductNo("PROD-2024-005");
            product5.setProductName("小额应急贷");
            product5.setProductType("个人消费贷款");
            product5.setMinAmount(BigDecimal.valueOf(500));
            product5.setMaxAmount(BigDecimal.valueOf(10000));
            product5.setMinTerm(1);
            product5.setMaxTerm(6);
            product5.setInterestRate(BigDecimal.valueOf(12.0));
            product5.setProductDescription("用于短期应急的小额贷款");
            product5.setStatus("active");
            product5.setCreatedAt(LocalDateTime.now());
            product5.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product5);

            log.info("产品数据初始化完成");
        }
    }

    private void initDemoUsers() {
        String demoPhone = "13800138000";
        if (userRepository.existsByPhoneNumber(demoPhone)) {
            return;
        }

        User demoUser = new User();
        demoUser.setUserId(UUID.randomUUID().toString().replace("-", ""));
        demoUser.setPhoneNumber(demoPhone);
        demoUser.setUserName("演示用户");
        demoUser.setIdCardNumber("110101199001011234");
        demoUser.setCreditScore(650);
        demoUser.setUserStatus(1);
        userRepository.save(demoUser);
        log.info("演示用户初始化完成：phone={}", demoPhone);
    }

    /** 跨月演示用户与贷款，使控制台图表更真实（幂等，仅初始化一次） */
    private void initHistoricalDemoData() {
        boolean seeded = loanApplicationRepository.findAll().stream()
                .anyMatch(a -> a.getApplicationNo() != null && a.getApplicationNo().startsWith("APP-SEED-"));
        if (seeded) {
            return;
        }

        int year = LocalDateTime.now().getYear();
        Object[][] users = {
                {"13800138001", "张伟", "110101199102031234", 720},
                {"13800138002", "李娜", "110101199203041234", 680},
                {"13800138003", "王芳", "110101199304051234", 640},
                {"13800138004", "刘洋", "110101199405061234", 710},
                {"13800138005", "陈静", "110101199506071234", 660},
        };

        Object[][] loans = {
                {"13800138001", "张伟", "个人消费贷款", "8000", 12, "approved", 1, 5},
                {"13800138002", "李娜", "个人消费贷款", "12000", 24, "pending", 2, 12},
                {"13800138003", "王芳", "个人经营贷款", "6000", 6, "rejected", 2, 20},
                {"13800138004", "刘洋", "个人消费贷款", "15000", 36, "approved", 3, 8},
                {"13800138005", "陈静", "个人消费贷款", "10000", 12, "approved", 4, 15},
                {"13800138001", "张伟", "小额应急贷", "3000", 3, "settled", 5, 3},
                {"13800138002", "李娜", "个人消费贷款", "9000", 12, "pending", 6, 10},
                {"13800138004", "刘洋", "个人经营贷款", "20000", 24, "approved", 6, 22},
                {"13800138003", "王芳", "个人消费贷款", "7500", 12, "rejected", 5, 18},
                {"13800138005", "陈静", "个人消费贷款", "11000", 18, "approved", 6, 2},
        };

        for (Object[] row : users) {
            String phone = (String) row[0];
            if (userRepository.existsByPhoneNumber(phone)) {
                continue;
            }
            User user = new User();
            user.setUserId(UUID.randomUUID().toString().replace("-", ""));
            user.setPhoneNumber(phone);
            user.setUserName((String) row[1]);
            user.setIdCardNumber((String) row[2]);
            user.setCreditScore((Integer) row[3]);
            user.setUserStatus(1);
            userRepository.save(user);
        }

        int seq = 1;
        for (Object[] row : loans) {
            String phone = (String) row[0];
            User user = userRepository.findByPhoneNumber(phone);
            if (user == null) {
                continue;
            }
            LoanApplication app = new LoanApplication();
            app.setApplicationNo(String.format("APP-SEED-%04d", seq++));
            app.setApplicantName((String) row[1]);
            app.setIdCardNumber(user.getIdCardNumber());
            app.setPhoneNumber(phone);
            app.setLoanType((String) row[2]);
            app.setLoanAmount(new BigDecimal((String) row[3]));
            app.setLoanTerm((Integer) row[4]);
            app.setStatus((String) row[5]);
            int month = (Integer) row[6];
            int day = (Integer) row[7];
            app.setApplyTime(LocalDateTime.of(year, month, day, 10, 30));
            app.setDescription("系统演示数据");
            app.setUser(user);
            loanApplicationRepository.save(app);
        }

        log.info("跨月演示贷款数据初始化完成");
    }

    /** 跨月已还记录，供「月度还款分析」图表展示 */
    private void initHistoricalRepayPlans() {
        boolean seeded = repayPlanRepository.findAll().stream()
                .anyMatch(p -> p.getPlanNo() != null && p.getPlanNo().startsWith("REPAY-SEED-"));
        if (seeded) {
            return;
        }

        List<LoanApplication> apps = loanApplicationRepository.findAll();
        if (apps.isEmpty()) {
            return;
        }

        int year = LocalDate.now().getYear();
        int maxMonth = LocalDate.now().getMonthValue();
        double[][] monthlyPaid = {
                {1, 5200}, {2, 8800}, {3, 7600}, {4, 10500}, {5, 9200}, {6, 11800},
                {7, 8400}, {8, 9600}, {9, 7300}, {10, 11200}, {11, 8900}, {12, 12500}
        };

        int seq = 1;
        for (double[] row : monthlyPaid) {
            int month = (int) row[0];
            if (month > maxMonth) {
                continue;
            }
            LoanApplication app = apps.get((seq - 1) % apps.size());
            BigDecimal total = BigDecimal.valueOf(row[1]);
            BigDecimal principal = total.multiply(BigDecimal.valueOf(0.88)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal interest = total.subtract(principal);

            RepayPlan plan = new RepayPlan();
            plan.setPlanNo(String.format("REPAY-SEED-%04d", seq++));
            plan.setLoanApplication(app);
            plan.setPeriodNo(1);
            plan.setDueDate(LocalDate.of(year, month, 15));
            plan.setPrincipal(principal);
            plan.setInterest(interest);
            plan.setTotalAmount(total);
            plan.setPaidPrincipal(principal);
            plan.setPaidInterest(interest);
            plan.setPaidTotal(total);
            plan.setStatus("PAID");
            repayPlanRepository.save(plan);
        }

        log.info("跨月演示还款数据初始化完成");
    }

    private void initDemoRepayPlan() {
        User demoUser = userRepository.findByPhoneNumber("13800138000");
        if (demoUser == null) {
            return;
        }

        List<RepayPlan> pendingPlans = repayPlanRepository.findPendingPlansByUserId(demoUser.getUserId());
        if (!pendingPlans.isEmpty()) {
            return;
        }

        LoanApplication loanApplication = new LoanApplication();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        loanApplication.setApplicationNo("APP-DEMO-" + timestamp);
        loanApplication.setApplicantName(demoUser.getUserName() != null ? demoUser.getUserName() : "演示用户");
        loanApplication.setIdCardNumber(demoUser.getIdCardNumber() != null ? demoUser.getIdCardNumber() : "110101199001011234");
        loanApplication.setPhoneNumber(demoUser.getPhoneNumber());
        loanApplication.setLoanType("个人消费贷款");
        loanApplication.setLoanAmount(new BigDecimal("5000.00"));
        loanApplication.setLoanTerm(12);
        loanApplication.setStatus("approved");
        loanApplication.setApplyTime(LocalDateTime.now().minusDays(5));
        loanApplication.setDescription("系统初始化生成的演示待还计划");
        loanApplication.setUser(demoUser);
        loanApplicationRepository.save(loanApplication);

        RepayPlan repayPlan = new RepayPlan();
        repayPlan.setPlanNo("PLAN-DEMO-" + timestamp);
        repayPlan.setLoanApplication(loanApplication);
        repayPlan.setPeriodNo(1);
        repayPlan.setDueDate(LocalDate.now().plusDays(7));
        repayPlan.setPrincipal(new BigDecimal("416.67"));
        repayPlan.setInterest(new BigDecimal("35.00"));
        repayPlan.setTotalAmount(new BigDecimal("451.67"));
        repayPlan.setPaidPrincipal(BigDecimal.ZERO);
        repayPlan.setPaidInterest(BigDecimal.ZERO);
        repayPlan.setPaidTotal(BigDecimal.ZERO);
        repayPlan.setStatus("PENDING");
        repayPlanRepository.save(repayPlan);

        log.info("演示待还计划初始化完成：userId={}, planNo={}", demoUser.getUserId(), repayPlan.getPlanNo());
    }

    /**
     * 为无风控报告的申请补跑评分卡（演示数据、历史数据直接入库时不会触发 submit 风控）。
     */
    private void backfillRiskReports() {
        try {
            dataCrawlerService.runFullCrawl();
        } catch (Exception e) {
            log.warn("启动时爬取外部数据失败，将使用适配器兜底：{}", e.getMessage());
        }

        List<LoanApplication> applications = loanApplicationRepository.findAll();
        int filled = 0;
        for (LoanApplication app : applications) {
            if (app.getId() == null || app.getUser() == null) {
                continue;
            }
            if (riskReportRepository.findByLoanApplication_Id(app.getId()).isPresent()) {
                continue;
            }
            try {
                riskService.performRiskAssessment(app.getId());
                filled++;
            } catch (Exception e) {
                log.warn("补全风控报告失败：applicationId={}, no={}", app.getId(), app.getApplicationNo(), e);
            }
        }
        if (filled > 0) {
            log.info("已为 {} 笔历史/演示申请补全风控报告", filled);
        }
    }
}