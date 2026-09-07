# 综设 II · 测试报告

**项目：** 多数据源融合互联网个人贷款风控系统 v1  
**测试日期：** 2026-06  
**测试类型：** 单元测试 + 功能联调（手工）

---

## 1. 测试环境

| 项 | 配置 |
|----|------|
| JDK | 21 |
| Spring Boot | 3.2.5 |
| 数据库 | MySQL 8.0 |
| 构建 | Maven 3.9 |
| 前端 | Vue3 / uni-app |

---

## 2. 单元测试汇总

执行命令：`cd backend && mvn test`

| 测试类 | 用例数 | 覆盖模块 | 结果 |
|--------|--------|----------|------|
| ContractServiceImplTest | 6 | 合同签署/放款 | 通过 |
| LoanApplicationServiceImplTest | 2 | 审批自动生成合同 | 通过 |
| SimpleScoringCardEngineTest | 2 | 评分卡计分 | 通过 |
| ScoringCardRiskEvaluatorTest | 2 | 评分卡阈值判定 | 通过 |
| DataCrawlerServiceTest | 1 | HTML 爬取解析 | 通过 |

**合计：13 用例，0 失败**

---

## 3. 功能测试用例（手工）

| ID | 场景 | 步骤 | 预期 | 结果 |
|----|------|------|------|------|
| TC-01 | 用户登录 | APP 输入手机号密码 | 返回 token，进入首页 | 通过 |
| TC-02 | 提交申请 | 填写金额期限提交 | 成功，返回 applicationNo | 通过 |
| TC-03 | 评分卡风控 | 提交后查进度/风控 API | 含 scoringCardPoints、passed | 通过 |
| TC-04 | 管理员审批 | Web 端通过申请 | 状态 approved，生成合同 | 通过 |
| TC-05 | 合同放款 | 签署 → 放款 | 状态 disbursed | 通过 |
| TC-06 | 用户还款 | APP 还款页点击还款 | 计划状态 PAID | 通过 |
| TC-07 | 数据爬取 | POST /api/admin/crawler/run | creditRows=10, telecomRows=10 | 通过 |
| TC-08 | Docker 部署 | deploy/deploy.sh | 80 端口可访问三端 | 通过 |

---

## 4. 风控专项测试

### 4.1 评分卡边界

- 信用分 720 + 贷款 3 万 → 总分 ≥ 40 → **通过**（单测 TC-01）  
-  mock 卡分 25 → **拒绝**（ScoringCardRiskEvaluatorTest）

### 4.2 多数据源融合

- 爬取后身份证尾号 `9` 的用户 → 征信 overdue=3 → 评分卡逾期维 -20 分  
- 运营商尾号 `4` → online_months=36 → +10 分  

---

## 5. 已知限制

- 爬取源为**课程演示 HTML**，非生产征信接口  
- Android APK 需 HBuilderX 云打包生成安装包  
- 部分用户端页面（人脸/银行卡）仍为演示 UI  

---

## 6. 结论

v1 核心链路（申请→风控→审批→合同→放款→还款）测试通过；评分卡与数据爬取模块具备自动化单测，满足综设 II 测试与模拟部署要求。
