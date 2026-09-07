# 综设 II · 后端总结报告代码引用建议

> 在 [`综设II-总结报告-后端部分.md`](./综设II-总结报告-后端部分.md) 中**插入 4 处**即可：每处对应一个 P 问题，篇幅短、答辩时可指读。Word 中可用「五号 Courier New / Consolas」或截图 IDE。

---

## 推荐总览

| 编号 | 插入章节 | 对应问题 | 文件 | 作用 |
|------|----------|----------|------|------|
| 代码 1-1 | 1.3 · P3 实现 | 提交即风控 | LoanApplicationServiceImpl | 证明风控绑定 submit |
| 代码 1-2 | 1.3 · P1/P2 实现 | 多源融合 + 规则链 | RiskServiceImpl | FeatureSnapshot + 规则遍历 |
| 代码 1-3 | 1.3 · P4 实现 | 审批后生成计划 | LoanApplicationServiceImpl | 状态机副作用时机 |
| 代码 1-4 | 1.3 · P4 实现 | 放款幂等 | ContractServiceImpl | 纠正「Redis 锁」误传 |

**可选第 5 处（写部署时）：** 1.3 · P5 → `deploy/nginx/nginx.conf` 的 `location /api/`（配置片段，非 Java）。

---

## 代码 1-1｜P3：提交申请时同步触发风控

**插入位置：** 1.3 节「P3：风控评估触发时机」实现段落，在文字说明「submitApplication() 内调用 performRiskAssessment」之后。

**引用说明（可写在代码上方 1～2 句）：**  
贷款申请保存为 pending 后，于同一事务流程内调用 RiskService，返回体附带 riskPassed，管理端与用户端可立即读取评估结果。

```java
// LoanApplicationServiceImpl.java — submitApplication()
application.setStatus("pending");
loanApplicationRepository.save(application);

// 触发风控评估（评分卡 + 规则链）
riskAssessment = riskService.performRiskAssessment(application.getId());

result.put("riskAssessment", riskAssessment);
result.put("riskPassed", riskAssessment.get("passed"));
```

**源码行号：** 约 206～230 行，`backend/src/main/java/.../LoanApplicationServiceImpl.java`

---

## 代码 1-2｜P1/P2：特征快照与规则链评估

**插入位置：** 1.3 节「P1 多源融合」或「P2 评分卡与规则链」实现段，说明适配器输出汇入 FeatureSnapshot 之后。

**引用说明：**  
RiskService 先经 FeatureContextService 合并库内与外部特征为 FeatureSnapshot，再按顺序执行各 RiskRuleEvaluator，结果写入 t_risk_report。

```java
// RiskServiceImpl.java — performRiskAssessment()
FeatureSnapshot featureSnapshot = featureContextService.build(user, application);
currentRiskFeatures.set(featureSnapshot);

for (RiskRuleEvaluator evaluator : enabledEvaluators) {
    Map<String, Object> ruleResult = evaluator.evaluateRisk(user, application);
    // ... 汇总 passed、riskScore、rejectReason
}
riskReport.setPassed(allPassed);
riskReport.setRejectReason(rejectReason);
riskReportRepository.save(riskReport);
```

**配套一句（可不贴代码）：** 外部数据经 `ExternalDataSourceAdapter` 接口接入，爬虫结果读自 `t_external_data_cache`：

```java
// ExternalDataSourceAdapter.java
public interface ExternalDataSourceAdapter {
    DataSourceType getSourceType();
    ExternalDataFetchResult fetch(DataSourceQuery query);
}
```

**源码行号：** RiskServiceImpl 约 66～129 行；接口见 `modules/risk/datasource/ExternalDataSourceAdapter.java`

---

## 代码 1-3｜P4：审批通过后再生成还款计划

**插入位置：** 1.3 节「P4 状态与一致性」中「还款计划生成时机」段之后。

**引用说明：**  
还款计划不在 submit 阶段创建，仅在 approveApplication() 中调用 ensureRepayPlansForApplication()，避免未审批贷款出现在用户还款页。

```java
// LoanApplicationServiceImpl.java — approveApplication()
application.setStatus("approved");
loanApplicationRepository.save(application);
contractService.createContractForApplication(application.getId());
ensureRepayPlansForApplication(application);
```

```java
// 查询层过滤：仅已审批及后续状态的贷款才返回待还计划
@Query("SELECT rp FROM RepaymentPlan rp WHERE rp.loanApplication.user.userId = :userId "
     + "AND rp.status IN ('PENDING', 'OVERDUE') "
     + "AND LOWER(rp.loanApplication.status) IN ('approved', 'disbursed', 'settled', 'paid')")
List<RepayPlan> findPendingPlansByUserId(@Param("userId") String userId);
```

**说明：** 第二段 JPQL 可与第一段二选一；若只要一处代码，**优先保留 approve 段**，JPQL 用文字概括即可。

**源码行号：** approve 约 76～89 行；JPQL 见 `RepayPlanRepository.java` 20～23 行

---

## 代码 1-4｜P4：放款幂等（数据库校验，非 Redis）

**插入位置：** 1.3 节「放款幂等」段；或文首「不符对照表」BUG-TC-004 说明旁注。

**引用说明：**  
重复放款前检查是否已有成功放款记录，存在则抛业务异常，不依赖 Redis 分布式锁。

```java
// ContractServiceImpl.java — disburseContract()
if (disburseRecordRepository.existsByContractAndDisburseStatus(
        contract, ContractStatus.DISBURSE_SUCCESS)) {
    throw new BusinessException("该合同已放款");
}
```

**源码行号：** 约 149～151 行，`service/impl/ContractServiceImpl.java`

---

## 可选代码 1-5｜P5：Nginx 同源反代（配置片段）

**插入位置：** 1.3 · P5 或「项目完成情况」部署段。

**引用说明：** 三端共用 80 端口，/api/ 反代 backend，与用户 H5 的 location / 分离配置。

```nginx
# deploy/nginx/nginx.conf
location /api/ {
    proxy_pass http://backend/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

---

## Word 排版建议

1. 代码块标题：**代码 1-1 提交申请时触发风控评估（节选）**  
2. 正文引用：「如代码 1-1 所示，……」  
3. 每段代码 **8～15 行** 为宜，过长可删日志与 catch  
4. 答辩时打开 IDE 同文件同行号，与报告一致  

## 不建议大量粘贴的部分

| 内容 | 原因 |
|------|------|
| SimpleScoringCardEngine 全部分箱 | 篇幅长，报告里表格已说明 |
| DataInitializer 种子数据 | 易误导为业务主流程 |
| 完整 docker-compose.yml | 用架构图更合适 |
| SecurityConfig 全文件 | 文字概括 JWT 即可 |

---

*行号以当前仓库为准，合并前可用 IDE 再核对一次。*
