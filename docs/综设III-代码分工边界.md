# 综设 III · 代码分工边界

**每周具体做什么、做到哪算完：** 以 `docs/综设III-12周开发计划.md` 第 1 节功能清单和第 4 节为准。

**发给组员的口径（可直接复制）：**  
3 个 Spring Boot = 网关 8088 + 主业务 8080（信贷和平台合在一起）+ 风控 8081（多模型成型后再拆）。不要再建 `loan-service` / `platform-service`。前端只打网关。  
风控定稿：**A/B/C/F 四卡**（申请/行为/催收/反欺诈）。A 卡不低于 II 评分卡。见 `docs/综设III-多模型风控设计.md`。  
客服：主业务 `modules/ops/` 调大模型辅助问答，无密钥降级 FAQ。  
钟妮：II 管理端已有，第 1 周改网关即压缩壳子；第 2 周起和岳一起做风控**报告页**，不改风控 Java。

**原则：** 每人只改自己目录里的文件。需要动别人的代码时，开 Issue / 群里说清，由**目录负责人**改，不要直接提交到对方目录。

**进程口径（3 个 Spring Boot）：** 独立网关 `gateway/`（8088）+ 主业务 `backend/`（信贷与平台同进程，8080）+ 风控独立服务（后拆，8081）。  
**不要**再建 `loan-service/`、`platform-service/`。拆风控之前，三人仍按下面路径在 `backend/` 里分开改。

---

## 一、总表

| 姓名 | 角色 | 只改这些（含其下所有子目录） |
|------|------|------------------------------|
| **邢芙** | 前端 APP | `apps/uni-client/` |
| **钟妮** | 前端 Web | `frontend/`、`apps/admin-web/`；**主攻风控报告/回放页**（与岳对 DTO，不改 Java） |
| **熊梓伊** | 后端信贷 | 见第二节「信贷」 |
| **岳炜杰** | 后端风控 | 见第二节「风控」 |
| **薛雨宸** | 后端平台 | 见第二节「平台」 |

前端两人**不要改** `backend/`（钟妮做风控也只改 Vue，字段问题开 Issue 给岳）。  
后端三人**不要改**对方模块包，也不要改两个前端目录。

---

## 二、后端按路径划分（尽量不重合）

以下路径均相对于：

`backend/src/main/java/com/example/zongshe1/`

### 熊梓伊 · 信贷（独占）

```
modules/user/
modules/auth/
modules/product/
modules/loanapplication/
modules/repay/
modules/settings/
modules/statistics/

controller/AdminContractController.java
controller/AdminUserController.java
controller/UserContractController.java
controller/PageController.java

service/ContractService.java
service/ContractServiceImpl.java
service/AdminUserService.java
service/AdminUserServiceImpl.java
repository/DisburseRecordRepository.java

dto/          （Admin*、PortalSettings 等管理端业务 DTO）
common/constants/
common/security/JwtUtil.java
common/config/JwtAuthenticationFilter.java
common/config/SecurityConfig.java
common/config/CorsConfig.java
common/config/WebConfig.java
common/config/SwaggerConfig.java
common/config/DataInitializer.java
common/exception/
common/util/
```

对应测试：

```
backend/src/test/java/.../LoanApplicationServiceImplTest.java
backend/src/test/java/.../ContractServiceImplTest.java
```

**可以调用、不准改：** 风控接口。过渡期可调 `RiskService`；风控迁到 8081 后只 HTTP 调 `POST /api/risk/assess`。  
**不准改：** 爬虫、风控实现、`deploy/`、`gateway/`、`frontend/`、`apps/uni-client/`。  
**不要**把信贷打成独立 `loan-service` 工程。

---

### 岳炜杰 · 风控（独占）

```
modules/risk/
modules/crawler/

backend/src/main/resources/crawler/
crawler/                          （根目录 Python 脚本）
```

对应测试：

```
backend/src/test/java/.../crawler/
backend/src/test/java/.../ScoringCardRiskEvaluatorTest.java
backend/src/test/java/.../SimpleScoringCardEngineTest.java
```

**可以调用、不准改：** `User`、`LoanApplication` 实体（只读字段算分）。需要新字段（如消费贷 `usage`）让**熊梓伊**加。  
**不准改：** 申请状态机、合同、放款、还款入账、`SecurityConfig`、`deploy/`。

以后新建文件也放在 `modules/risk/` 或 `modules/crawler/` 下，例如 `WoeScoringCardEngine`、`AhpWeightCalculator`、`TwoStageRiskOrchestrator`、失信适配器、行为分。详见 `docs/综设III-多模型风控设计.md`。多模型能演示后再迁到独立 `risk-service`（8081），不要提前为拆而拆。

---

### 薛雨宸 · 平台（独占）

平台与信贷**同进程**（都在 `backend/`），网关是**另一个** Spring Boot。不要去改信贷/风控业务类，也不要新建 `platform-service`。

```
gateway/                          （独立进程 8088，已存在）
deploy/                           （含 docker-compose、nginx、.env.example）
.dockerignore
backend/Dockerfile
backend/Dockerfile.prebuilt

backend/.../modules/ops/         审计、催收、工单、FAQ、AI 客服辅助、渠道、预警、缓存切面、限流
```

缓存、RabbitMQ、Gateway：写成**新类、新配置段、新包**，不要改 `RiskServiceImpl`、`RepaymentServiceImpl`、`LoanApplicationServiceImpl` 的业务逻辑。

需要信贷发消息、风控发消息时：

- 薛雨宸：声明队列、提供「怎么发」的小工具类（放 `modules/ops/` 或 `common/mq/`，由薛维护）
- 熊 / 岳：只在自己的 Service 里**调用**该工具，不改工具内部

**不准改：** `modules/risk/`、`modules/crawler/`、申请/还款/合同实现、两个前端目录。

---

## 三、必须共用的文件（最容易撞车）

这些人少、文件又只有一份，规定**主责人 + 别人怎么动**：

| 文件 | 主责 | 别人怎么做 |
|------|------|------------|
| `backend/pom.xml` | 薛雨宸 | 熊/岳要加依赖：在群里说名称和用途，**薛来改**（或 PR 只动自己那几行 dependency，合并前薛看一眼） |
| `backend/src/main/resources/application.properties` | 按段划分，见下 | 只改自己那一段 |
| `backend/src/main/resources/application-prod.properties` | 同 properties | 同左 |
| `backend/src/main/resources/schema.sql` | 谁的表谁加，合并前互看 | 熊：用户/申请/合同/还款；岳：风控/快照；薛：审计/催收/工单/渠道（第 9 周起与信贷同 `loan_db`，风控表进 `risk_db`） |
| `backend/src/main/java/.../Zongshe1Application.java` | 薛雨宸 | 一般不用改；要加 `@EnableScheduling` 等再找薛 |
| `docs/综设III-*.md` | 薛雨宸维护计划类 | 各人可改自己模块说明，不要大段重写别人的周任务 |

### `application.properties` 分段（建议立刻在文件里加注释）

```
# ===== 信贷（仅熊梓伊改）=====
# 数据源默认、JWT、业务开关、最低信用分等

# ===== 风控（仅岳炜杰改）=====
# risk.*  crawler.*  datasource.*

# ===== 平台（仅薛雨宸改）=====
# Redis 主机、以后的 rabbitmq、限流、cache
```

现有 Redis 配置给平台用；JWT 密钥、数据库账号给信贷用。岳炜杰不要改 `DB_` / `JWT_`。

---

## 四、接口怎么对接（不改对方代码）

| 调用 | 谁提供 | 谁使用 |
|------|--------|--------|
| 登录 / 注册 / 产品 / 申请 / 审批 / 合同 / 放款 / 还款 | 熊梓伊 | 邢芙、钟妮 |
| `POST` 风控评估、回放、重评、特征查询、手动爬取 | 岳炜杰 | 熊梓伊（申贷后评估）；薛雨宸（重评、爬取按钮转发）；**钟妮（第 2 周起做报告/回放页，与岳联调）** |
| 网关地址、缓存、队列、催收/工单/FAQ/AI 客服/渠道/审计 | 薛雨宸 | 前端改 baseURL；熊/岳只发事件 |

多模型怎么拆、怎么融合、何时迁 8081：见 `docs/综设III-多模型风控设计.md`。

前端 **只调 HTTP**，不直接改 Java。  
后端之间 **只调对方已公开的方法/URL**，不进对方类里改实现。

---

## 五、Git 分支（和目录一致）

| 分支 | 人 | 应出现的路径 |
|------|----|----------------|
| `feature-xing-app` | 邢芙 | 仅 `apps/uni-client/` |
| `feature-zhong-web` | 钟妮 | 仅 `frontend/`、`apps/admin-web/` |
| `feature-xiong-loan` | 熊梓伊 | 仅信贷路径 + 自己的测试 |
| `feature-yue-risk` | 岳炜杰 | 仅风控/爬虫路径 + 自己的测试 |
| `feature-xue-platform` | 薛雨宸 | 仅 `deploy/`、`gateway/`、`modules/ops/`、Dockerfile、pom 依赖 |

PR 里如果出现「不该出现的路径」，合并前打回去自己改。

例外：`application.properties` / `pom.xml` / `schema.sql` 允许出现在对应主责人的 PR 里；跨段改动必须在 PR 说明里写清并 @ 主责人。

---

## 六、禁止事项（减少重合）

1. 不要把新功能写进别人已经在改的大类里（例如催收不要写进 `RepaymentServiceImpl`，应新建 `modules/ops/...`）。  
2. 不要三个人同时改 `SecurityConfig`。鉴权过滤仍归熊；限流做 Gateway 新类，归薛。  
3. 不要前端为了联调直接改后端返回字段名；要改契约时：前端提出 → 对应后端改 → 再对接。  
4. 不要提交 `node_modules`、`backend/target`、`.env`、真实密码、`AI_CS_API_KEY`。

---

有冲突时以**目录负责人**的版本为准，需要的字段由负责人加，调用方等接口就绪再接。
