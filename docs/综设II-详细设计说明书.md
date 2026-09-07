# 综设 II · 详细设计说明书

## 1. 风控详细流程（集成单元过程）

### 1.1 贷款申请触发风控时序

```
用户(APP) -> LoanApplicationController.submit
         -> LoanApplicationServiceImpl.submitApplication
         -> FeatureContextService.build
              -> ExternalDataOrchestrator.fetchAll
                   -> MockCreditBureauAdapter (读爬取缓存)
                   -> MockTelecomAdapter (读爬取缓存)
         -> RiskServiceImpl.performRiskAssessment
              -> ScoringCardRiskEvaluator (评分卡)
              -> BasicRiskRuleEvaluator
              -> ExternalDataRiskEvaluator
         -> RiskReportRepository.save
         -> 返回 riskAssessment 给 APP/管理端
```

### 1.2 评分卡计分（SimpleScoringCardEngine）

| 特征 | 分箱 | 分值 |
|------|------|------|
| credit_score | ≥700 / ≥600 / ≥500 / 其他 | 50/35/20/5 |
| loan_amount | ≤5万 / ≤20万 / 更高 | 30/20/10 |
| credit_overdue_count（爬取） | 0 / ≤2 / >2 | +10 / 0 / -20 |
| telecom_online_months（爬取） | ≥24 / ≥12 / 其他 | +10 / +5 / 0 |

通过阈值：`risk.scoring-card.pass-min-points=40`

### 1.3 数据爬取与融合

1. `DataCrawlerScheduler` 定时执行（或 `POST /api/admin/crawler/run`）  
2. `DataCrawlerService` 用 Jsoup 解析 `demo-credit.html` / `demo-telecom.html`  
3. 写入 `t_external_data_cache`（按身份证/手机尾号索引）  
4. 适配器 `fetch()` 优先读缓存，无缓存则算法兜底  

Python 独立脚本：`crawler/crawl_external_data.py`（结构校验 + 可选触发 API）

---

## 2. 核心业务类设计

| 类 | 职责 |
|----|------|
| `ContractServiceImpl` | 合同生成/签署/放款 |
| `RepaymentServiceImpl` | 还款计划查询与还款入账 |
| `RepaymentOverdueScheduler` | 每日检测逾期并计罚息 |
| `ExternalDataCacheService` | 爬取结果 CRUD |
| `ScoringCardEngine` | 评分卡接口 |
| `RiskRuleEvaluator` | 规则链接口 |

---

## 3. 接口设计（摘录）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/users/login | 用户登录 |
| POST | /api/loan-applications/submit | 提交申请（触发风控） |
| GET | /api/risk/applications/{id}/assessment | 风控报告 |
| POST | /api/admin/crawler/run | 触发爬取 |
| POST | /api/admin/contracts/{id}/disburse | 放款 |

完整列表见 Swagger：`/swagger-ui.html`

---

## 4. 借款端页面设计

| 页面 | 文件 | 调用 API |
|------|------|----------|
| 登录 | login.vue | POST /api/users/login |
| 首页 | home.vue | GET /api/credit/.../evaluation |
| 申请 | apply.vue | POST /api/loan-applications/submit |
| 进度 | progress.vue | GET /api/loan-applications/user/{id} |
| 还款 | repay.vue | GET/POST /api/repayment/... |

公共模块：`src/utils/api.js`

---

## 5. 数据库新增表

**t_external_data_cache** — 爬取数据缓存

| 字段 | 说明 |
|------|------|
| source_type | CREDIT_BUREAU / TELECOM |
| id_card_number / phone_number | 索引键（SUFFIX_x） |
| payload_json | 特征 JSON |
| crawled_at / expires_at | 时效 |

---

## 6. 非功能设计

- **安全：** JWT、BCrypt、环境变量外置敏感配置  
- **测试：** JUnit5 + Mockito，覆盖合同/评分卡/爬取  
- **部署：** Docker Compose，见 `deploy/`
