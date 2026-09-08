# 综设 III · 代码分工边界

**原则：** 每人只改自己目录里的文件。需要动别人的代码时，开 Issue / 群里说清，由**目录负责人**改，不要直接提交到对方目录。

**当前仓库仍是单体**（`backend/` 一个工程）。还没拆成三个服务之前，也按下面路径分开改，避免五个人改同一文件。

---

## 一、总表

| 姓名 | 角色 | 只改这些（含其下所有子目录） |
|------|------|------------------------------|
| **邢芙** | 前端 APP | `apps/uni-client/` |
| **钟妮** | 前端 Web | `frontend/`、`apps/admin-web/` |
| **熊梓伊** | 后端信贷 | 见第二节「信贷」 |
| **岳炜杰** | 后端风控 | 见第二节「风控」 |
| **薛雨宸** | 后端平台 | 见第二节「平台」 |

前端两人**不要改** `backend/`。  
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

**可以调用、不准改：** `modules/risk/` 里的接口（只 `RiskService.performRiskAssessment(...)`）。  
**不准改：** 爬虫、风控实现、`deploy/`、`frontend/`、`apps/uni-client/`。

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

以后新建文件也放在 `modules/risk/` 或 `modules/crawler/` 下，例如失信适配器、回放接口、行为分。

---

### 薛雨宸 · 平台（独占）

现有仓库里平台几乎是新建，不要去改信贷/风控业务类。

```
deploy/                           （含 docker-compose、nginx、.env.example）
.dockerignore
backend/Dockerfile
backend/Dockerfile.prebuilt

新建（不要塞进信贷/风控包）：
  gateway/                        （以后独立模块）
  backend/.../modules/ops/        或 modules/platform/
      审计、催收任务、工单、FAQ、渠道、预警、缓存切面、限流
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
| `backend/src/main/resources/schema.sql` | 谁的表谁加，合并前互看 | 熊：用户/申请/合同/还款；岳：风控/缓存/快照；薛：审计/催收/工单/渠道 |
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
| `POST` 风控评估、回放、重评、特征查询、手动爬取 | 岳炜杰 | 熊梓伊（申贷后评估）；薛雨宸（重评、爬取按钮转发）；钟妮（报告页） |
| 网关地址、缓存、队列、催收/工单/渠道/审计 | 薛雨宸 | 前端改 baseURL；熊/岳只发事件 |

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
| `feature-xue-platform` | 薛雨宸 | 仅 `deploy/`、Dockerfile、新建 ops/gateway、pom 依赖 |

PR 里如果出现「不该出现的路径」，合并前打回去自己改。

例外：`application.properties` / `pom.xml` / `schema.sql` 允许出现在对应主责人的 PR 里；跨段改动必须在 PR 说明里写清并 @ 主责人。

---

## 六、禁止事项（减少重合）

1. 不要把新功能写进别人已经在改的大类里（例如催收不要写进 `RepaymentServiceImpl`，应新建 `modules/ops/...`）。  
2. 不要三个人同时改 `SecurityConfig`。鉴权过滤仍归熊；限流做 Gateway 新类，归薛。  
3. 不要前端为了联调直接改后端返回字段名；要改契约时：前端提出 → 对应后端改 → 再对接。  
4. 不要提交 `node_modules`、`backend/target`、`.env`、真实密码。

---

有冲突时以**目录负责人**的版本为准，需要的字段由负责人加，调用方等接口就绪再接。
