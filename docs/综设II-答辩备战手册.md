# 综设 II 答辩备战手册（四端 + 风控/爬虫/部署详解）

> 建议答辩前全组通读一遍；每人重点看自己负责段落 + **第四节（风控/爬虫/部署）** + **第五节（综合 Q&A）**。

---

## 一、四部分：已完成内容 & 未来改进

### 1. 管理端前端（钟妮）

**已完成（约 85%）**

| 模块 | 具体内容 |
|------|----------|
| 登录 | 账号密码登录，Token 存 localStorage/sessionStorage，跳转 `/admin/` |
| 布局 | 左侧菜单 + 右侧内容区：控制台、贷款、合同、产品、用户、还款、设置等 |
| 控制台 | 对接 `/api/statistics/dashboard`、`/charts`；ECharts 展示申请趋势、状态分布、**月度还款柱图**（已修布局与数据刷新） |
| 贷款管理 | 列表展示申请人、金额、期限、状态；**风控列**（通过/未通过、评分卡分数、拒绝原因）；通过/拒绝操作，风控未通过时有二次确认 |
| 合同管理 | 列表、签署、放款；状态标签区分 |
| 产品管理 | 产品 CRUD |
| 技术栈 | **Vue 3 + Vite + ECharts + fetch**（`api/client.js`、`api/admin.js` 封装） |

**未来改进**

- 控制台增加「一键触发爬取」按钮（目前需 Swagger/curl 调 `POST /api/admin/crawler/run`）
- 审批/操作后列表刷新、loading 防抖再统一
- 空数据态、401 跳转登录等异常提示再细化
- 报告里若写了 Element Plus / Axios，答辩口径统一为 **Vue3 + 自定义样式 + fetch**

---

### 2. 用户端前端（邢芙）

**已完成（约 85%）**

| 页面/模块 | 具体内容 |
|-----------|----------|
| 登录注册 | `login-prototype.html`：密码/验证码登录；两步注册；Token 存 localStorage |
| 首页 | `main-home.html`：可借额度（调 `/api/credit/...`）、功能网格（还款/额度/资料/借款记录）、退出登录 `/?logout=1` |
| 借款 | `loan-home.html`：金额、期限、还款方式、**实时还款计算器**（等额本息/等额本金） |
| 借款记录 | `loan-records.html`：申请列表、进度条、风控结果；状态中文（已通过/已放款等，已修 `disbursed`） |
| 还款 | `repay.html`：待还计划（全额/部分）、还款记录筛选；跳转 `payment.html` 二次确认 |
| 额度 | `credit-limit.html`：信誉分 SVG 环、增信资料提交 |
| 技术 | **原生 HTML + CSS + ES6+ MPA**；`js/user-session.js`、`js/api-config.js` 统一请求与同源 API |
| 部署访问 | 云服务器根路径 `http://47.109.202.165/` |

**演示账号：** 13800138000 / 123456

**未来改进**

- 小屏（<360px）筛选区布局再适配
- 增信提交、部分场景 pageshow 刷新再完善
- uni-app 安卓包（HBuilderX 云打包）作为扩展交付
- 答辩表述：**H5 是云上主演示形态**；首页是**功能网格**，不是底部四 Tab（除非讲 uni-app 版）

---

### 3. 后端（后端负责人）

**已完成（约 85%）**

| 子系统 | 具体内容 |
|--------|----------|
| 认证 | JWT；用户登录/注册/验证码（Redis）；管理员登录（`.env` 的 `ADMIN_PASSWORD`） |
| 贷款 | 提交、列表、审批/拒绝；**提交即风控**；**审批通过才生成还款计划** |
| 风控 | 评分卡 + 规则链；报告入 `t_risk_report`；DTO 含 `riskPassed`、`scoringCardPoints`、`statusText` |
| 爬虫 | Jsoup 解析演示 HTML → `t_external_data_cache`；定时 + `POST /api/admin/crawler/run` |
| 合同/放款 | 审批后生成合同 → 签署 → 放款；重复放款 DB 校验 |
| 还款 | 计划生成、全额/部分还款、逾期定时任务 |
| 统计 | 控制台数据；月度还款按 **实还时间** 统计；最近贷款状态含 `disbursed` |
| 部署 | Docker Compose + Nginx 同源；`settings.xml` 阿里云 Maven；`Dockerfile.prebuilt` 备选 |

**未来改进**

- 管理端爬取按钮对接已有 API
- Redis 业务缓存（当前 mainly 验证码）
- 真实征信/运营商 API 替换 Mock 适配器
- HTTPS、日志与监控

---

### 4. 测试（岳炜杰 + 熊梓伊）

**已完成**

| 层级 | 内容 | 结果 |
|------|------|------|
| **单元测试** | JUnit5 + Mockito；5 类 **13 用例** | `mvn test` **13/13 通过** |
| **风控专项** | RC01–RC10 用例设计；Apifox + Navicat | 主场景已测（如 RC-06 爬取前后分数） |
| **功能测试** | TC01–TC18；登录、申贷、审批、放款、还款、爬取、Docker | 主链路通过（见 `docs/综设II-测试报告.md`） |

**单元测试覆盖模块：** 合同 6 例、贷款 2 例、评分卡/风控 4 例、爬虫 1 例

**未来改进**

- RC/TC 用例文档与截图归档进终稿
- 自动化接口测试（Postman/Apifox 集合 CI）
- 弱网、并发点击等边界用例补充
- 答辩勿写「放款 Redis 分布式锁」——代码是 **数据库放款记录校验**

---

## 二、风控模型（答辩重点 · 讲清楚「是什么、怎么跑」）

### 2.1 整体思路：不是黑盒 AI，是「可解释的评分卡 + 规则链」

```
用户提交贷款
    ↓
收集特征（库内 + 爬取缓存）
    ↓
规则链逐条评估（含评分卡）
    ↓
写入 risk_reports（分数、是否通过、原因）
    ↓
管理端 / 用户端展示
```

**为什么不用机器学习？**  
样本少、答辩要讲清「为什么拒」；评分卡每一维加减分都能说明，符合课程「评分卡机制」要求。

### 2.2 特征从哪来（多数据源融合）

| 来源 | 字段示例 | 怎么进系统 |
|------|----------|------------|
| **库内** | 用户信用分、申请金额 | 直接读 `users`、`loan_applications` |
| **征信（演示）** | 逾期次数、30 天查询次数 | 爬虫 → 缓存 → `MockCreditBureauAdapter` |
| **运营商（演示）** | 在网月数、是否实名 | 爬虫 → 缓存 → `MockTelecomAdapter` |

中间统一成 **`FeatureSnapshot`**（特征快照），后面评分卡和规则都只读这一份，换数据源不用改评分逻辑。

**匹配规则（演示）：** 按身份证/手机 **末位 0–9** 对应演示 HTML 表格里的一行（如尾号 9 → overdue=3）。

### 2.3 规则链（谁先谁后、干什么）

Spring 自动注入多个 `RiskRuleEvaluator`，按优先级执行，**任一不通过则整体不通过**：

1. **BasicRiskRuleEvaluator**
   - 信用分 < 450 → 直接拒
   - 用户状态异常 → 直接拒

2. **ScoringCardRiskEvaluator**
   - 调评分卡算总分，和 **40 分阈值** 比（配置项 `risk.scoring-card.pass-min-points=40`）

3. **ExternalDataRiskEvaluator**
   - 如征信逾期次数 **> 2**（阈值 2）→ 硬性拒绝

结果写入 **`t_risk_report`**：`riskScore`、`scoringCardPoints`、`scoringCardMax`、`passed`、`rejectReason` 等。

### 2.4 什么时候触发风控？

- **用户提交申请时**：自动 `performRiskAssessment()`（管理端不会长期「未评估」）
- **爬取后演示**：`POST /api/risk/applications/{id}/assess` **手动重算**（RC-06）
- **系统启动**：演示种子数据 `backfillRiskReports()` 补历史报告

---

## 三、评分卡（答辩重点 · 带例子讲）

**引擎类：** `SimpleScoringCardEngine`  
**满分：** 100  
**通过线：** 40 分（可配置）

### 3.1 四个维度怎么打分

| 维度 | 含义 | 分箱示例 | 分值 |
|------|------|----------|------|
| 信用分 | 用户信用分 | ≥700 / ≥600 / ≥500 / 更低 | 50 / 35 / 20 / 5 |
| 贷款金额 | 申请金额 | ≤5万 / ≤20万 / 更高 | 30 / 20 / 10 |
| 征信逾期 | 爬取缓存 | 0 次 / ≤2 次 / >2 次 | +10 / 0 / **−20** |
| 运营商在网 | 爬取缓存 | ≥24 月 / ≥12 月 / 更短 | +10 / +5 / 0 |

每一维都有 **breakdown 分项**，管理端能看到「哪一项加/减分」，这就是**可解释性**。

### 3.2 答辩举例（建议背一个）

**例子 A — 容易通过：**

信用分 720 → 50 分；贷款 3 万 → 30 分；无逾期 +10；在网 36 月 +10 → **合计 100 分（封顶）→ 通过**

**例子 B — 爬取后变拒（RC-06）：**

同样 50+30=80 基础分，但身份证尾号 **9** 爬取后 overdue=3 → 逾期维 **−20** → **60 分仍可能通过**；若同时触发「逾期>2 硬性规则」或分数叠加后 <40 → **拒绝**

（具体以当时规则和缓存为准，答辩时说「爬取会改变逾期维得分，从而可能改变 passed」即可）

### 3.3 评分卡 vs 规则链

- **评分卡**：算一个 **0–100 的分**，和 40 比，适合展示「80/100」
- **规则链**：硬性条件（信用分门槛、逾期黑名单）
- 两者 **都要**，不是二选一

---

## 四、爬虫（答辩重点 · 演示怎么说）

### 4.1 做什么

在**不能接真实征信 API** 的课程环境下，用 **演示 HTML 页面** 模拟外部数据源，用 **Jsoup** 当爬虫解析表格，结果入库，供风控读取。

### 4.2 流程（逐步讲）

```
1. 数据源文件
   backend/src/main/resources/crawler/demo-credit.html（征信）
   demo-telecom.html（运营商）
   也可改成 HTTP URL（配置 crawler.credit-bureau.url）

2. DataCrawlerService.runFullCrawl()
   Jsoup 选择器：table#credit-data tbody tr
   每行：id_suffix、overdue_count、query_count_30d 等

3. 写入 t_external_data_cache
   键：SUFFIX_0 ~ SUFFIX_9
   TTL：24 小时（crawler.cache-ttl-hours=24）

4. 用户申请风控时
   MockCreditBureauAdapter / MockTelecomAdapter 读缓存
   无缓存 → 末位算法兜底

5. FeatureSnapshot → 评分卡 / 外部规则
```

### 4.3 怎么触发

| 方式 | 说明 |
|------|------|
| **定时** | `DataCrawlerScheduler`，启动约 2 分钟后首次，之后约 6 小时 |
| **手动（答辩推荐）** | 管理员 Token + `POST /api/admin/crawler/run` |
| **启动** | `DataInitializer` 里可 `runFullCrawl()` 再 `backfillRiskReports()` |

**成功返回示例：** `creditRows: 10, telecomRows: 10`

### 4.4 答辩演示话术（1 分钟）

> 「我们先提交一笔贷款，系统会用库内信用分和金额算一版风控。然后管理员触发爬取接口，把演示页里的征信、运营商表格解析进缓存。对身份证尾号 9 的用户，逾期次数会变成 3，评分卡逾期维扣 20 分，再调用 assess 重算，可以看到 passed 和 scoringCardPoints 变化。这模拟了生产里『外部数据更新 → 风控重算』的流程，真正上线只需把 Adapter 换成真实 API。」

---

## 五、云服务器部署流程（答辩重点 · 逐步）

**环境示例：** 阿里云 `47.109.202.165`，目录 `/opt/zongshe`

### 5.1 架构（一张图口述）

```
手机/PC 浏览器
    → Nginx:80（web 容器）
        → /          用户 H5（apps/uni-client/*.html）
        → /admin/    管理端 Vue 静态包
        → /api/      反代 backend:8080
    → backend 容器（Spring Boot JAR）
        → mysql 容器（业务库 zongshe）
        → redis 容器（验证码等）
```

**同源好处：** 用户 H5 用相对路径 `/api/...`，不用在手机里配 IP。

### 5.2 部署步骤（简版，老师问「怎么部署」照此答）

**① 本机上传代码（Windows）**  
只传 `backend/`、`frontend/`、`apps/`、`deploy/`，不要传 `node_modules`。

**② 服务器准备**  
安装 Docker + Docker Compose；开放 **80** 端口；建议加 swap（backend Maven 构建很吃内存）。

**③ 配置环境变量**

```bash
cd /opt/zongshe/deploy
cp .env.example .env
# 编辑：DB_PASSWORD、JWT_SECRET、ADMIN_PASSWORD 等
# 注意：.env 不要用 # 注释行（曾导致 Docker 解析失败）
```

**④ 构建与启动**

```bash
docker compose up -d --build
# 或分开：先 mysql/redis，再 backend，再 web
docker compose ps   # 四个容器都应 Up
```

**⑤ backend 构建慢或失败时**

- 加 `backend/settings.xml`（阿里云 Maven 镜像）
- 或本机 `mvn package` 后 `Dockerfile.prebuilt` 只拷 JAR

**⑥ 改代码后更新**

- 只改前端：`docker compose build web && docker compose up -d web`
- 只改后端：`docker compose build backend && docker compose up -d --force-recreate backend`
- 改 `.env`：**必须** `force-recreate backend`，`restart` 不够

**⑦ 验证**

- `http://IP/` 用户端
- `http://IP/admin/` 管理端
- `http://IP/swagger-ui.html` API 文档

### 5.3 常见问题（部署向）

| 现象 | 原因 | 处理 |
|------|------|------|
| backend 反复重启 | `.env` 与 MySQL 密码不一致 | 对齐密码或 `down -v` 重建（会清库） |
| Maven Network unreachable | 容器内连不上 Maven Central | `settings.xml` 或本机打 JAR |
| 管理端能登、API 401 | Token 过期或未带 Bearer | 重新登录 |
| 控制台图表空白 | 未拉 statistics 或 ECharts 未 refresh | 刷新页面、看 network |

---

## 六、分模块：答辩可能问什么 & 怎么答

### 6.1 管理端

**Q：管理端用什么框架？**  
A：Vue 3 + Vite + ECharts，请求用 **fetch** 封装在 `api/admin.js`，没有用 Element Plus。表格和弹窗是 Dashboard 里自定义组件和样式。

**Q：风控信息从哪来？**  
A：贷款列表接口返回 DTO 里的 `riskPassed`、`riskScore`、`scoringCardPoints`、`rejectReason`，来自后端 `t_risk_report`，不是前端算的。

**Q：审批通过后前端还要做什么？**  
A：调 `approve` 接口后重新 `loadLoans()`、刷新控制台；后端会自动生成合同和还款计划，前端合同页再操作签署/放款。

**Q：图表数据是真的吗？**  
A：是的，来自 `/api/statistics/charts` 等，不是写死的假数组（开发阶段曾用默认数据，联调后接 API）。

---

### 6.2 用户端

**Q：为什么用 HTML 不用 Vue/uni-app？**  
A：H5 轻量、免编译、部署简单，Nginx 直接静态托管；uni-app 在仓库里有扩展，**答辩演示以云上 H5 为主**。

**Q：登录态怎么保持？**  
A：JWT 存 localStorage，`user-session.js` 请求头带 `Authorization: Bearer`；退出用 `/?logout=1` 清本地。

**Q：为什么有时还能看到英文状态 disbursed？**  
A：已做 `statusText` 映射；若仍出现，是旧缓存或未部署最新 `loan-records.html`，刷新或清站点数据。

**Q：没审批为什么以前能看到还款计划？**  
A：早期后端提交时就生成计划，已改为 **审批通过后** 才生成；查询也过滤了贷款状态，现在不应再出现。

---

### 6.3 后端

**Q：整体架构？**  
A：前后端分离，单体 Spring Boot，按 modules 分包；MySQL 持久化，Redis 验证码；JWT 鉴权。

**Q：贷款状态有哪些？**  
A：`pending` → `approved` → 合同流程 → `disbursed` → 还清 `SETTLED`；拒绝 `rejected`。

**Q：如何保证事务一致？**  
A：审批、放款、还款等写操作用 `@Transactional`；放款前查是否已有成功放款记录，防重复。

**Q：Swagger 干什么用？**  
A：接口文档和答辩演示调爬取、assess 等管理接口。

---

### 6.4 测试

**Q：测了多少？**  
A：自动化 **13** 条单元测试全过；功能手工 TC、风控 RC 用例设计并在联调中验证主场景。

**Q：为什么单元测试不连数据库？**  
A：Mock Repository，只测 Service 逻辑，快、稳定；集成靠 RC/TC + Docker 环境。

**Q：UT 发现了什么 bug？**  
A：例如跨用户签署合同、未审批生成合同等，在单测阶段暴露，比全链路测再发现成本低。

**Q：RC-06 怎么测？**  
A：尾号 9 用户申请 → 爬取 → assess 重算 → 对比 `scoringCardPoints` 和 `passed`，Navicat 查 `t_external_data_cache` 和 `t_risk_report`。

---

## 七、综合高频题（全组都要会）

| 问题 | 简答要点 |
|------|----------|
| 课题创新点？ | 多源融合链路可演示；评分卡可解释；三端同源 Docker 部署 |
| 和生产差距？ | 爬取页是演示；适配器可换真 API；人脸/银行卡部分为 UI 演示 |
| 数据安全？ | JWT、密码 BCrypt、敏感项进 `.env` 不进 Git |
| 团队怎么协作？ | Swagger 定接口；前后端字段对齐；测试提前报 bug |
| 15 分钟演示讲什么？ | 登录 → 申贷 → 管理端看风控 → 爬取+重算 → 审批 → 放款 → 还款 → 控制台 |
| 参考文献为何不对？ | 终稿应换 Spring Boot、软件测试、数据库、个保法相关文献 |

---

## 八、答辩前 30 分钟 checklist

- [ ] `docker compose ps` 四容器 **Up**
- [ ] `http://47.109.202.165/` 和 `/admin/` 能开
- [ ] 用户 **13800138000 / 123456**；管理端 **yunizai / .env 密码**
- [ ] 需要干净登录：`http://IP/?logout=1`
- [ ] 准备好管理员 Token（Swagger 登录后复制）调 **爬取** 和 **assess**
- [ ] 准备讲 **尾号 9** 的爬取演示（或现场选一笔申请）
- [ ] 本机备 **`mvn test` 截图**、**架构图**（见 `docs/综设II-答辩PPT-逐页内容.md`）
- [ ] 口径统一：**无 Element Plus、无 Axios、无 Redis 放款锁、H5 非底部 Tab**

---

## 九、每人 1 分钟自述模板（可选）

- **后端：** 负责 Spring Boot、风控评分卡、爬虫融合、Docker 部署；解决提交即风控、审批后还款计划、云构建等问题。
- **钟妮：** 负责 Vue3 管理端，控制台 ECharts、贷款审批与风控展示、合同产品管理。
- **邢芙：** 负责 H5 用户端全流程，实时计算器、还款确认、借款记录与进度展示。
- **岳炜杰：** 负责三层测试设计，完成 13 条单元测试与 mvn test 全通过。
- **熊梓伊：** 负责 RC/TC 用例与 Apifox、Navicat 联调，验证风控规则与全链路。

---

## 十、相关文档索引

| 文档 | 用途 |
|------|------|
| `docs/综设II-答辩PPT-逐页内容.md` | PPT 逐页文案与演示脚本 |
| `docs/综设II-总结报告-后端部分.md` | 后端章节与七条推理对应 |
| `docs/综设II-测试报告.md` | TC 手工用例 |
| `docs/综设II-团队分工说明.md` | 分工说明 |
| `deploy/README.md` | 部署操作说明 |

---

祝答辩顺利。
