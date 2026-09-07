# 综设 II · 总结报告（后端部分）

> 本文档对应团队总结报告中「后端开发」各章节正文，格式与钟妮（管理端）、邢芙（用户端）、岳炜杰/熊梓伊（测试）章节并列。请将 **第四章分工** 中补上后端负责人姓名后合并入正式 Word。

---

## 【请先阅读】组员报告中与项目实际不符之处（合并前建议修改）

以下根据当前仓库代码 d:\ZongShe2 核对，避免答辩被老师追问时前后矛盾：

| 位置 | 原文表述 | 实际情况 | 建议修改 |
|------|----------|----------|----------|
| 管理员前端 1.1 | 技术栈为 **Vue3 + Element Plus + ECharts + Axios** | frontend/package.json 仅有 vue、echarts、vite；**无 Element Plus、无 Axios**；表格/表单为 Dashboard.vue **自定义 CSS**；请求为 fetch（api/client.js） | 改为「Vue3 + ECharts + 原生 fetch」 |
| 用户端 1.1 | 选型理由「**与管理员端技术栈保持一致**」 | 管理端 Vue3 SPA，用户端 **原生 HTML MPA**，技术栈并不一致 | 删除该理由，改为「H5 轻量、免编译、答辩部署方便」 |
| 用户端 1.1 | 「**底部 Tab 导航（首页、借款、还款、我的）**」 | 部署的 H5（main-home.html 等）为 **首页功能网格 + 页面跳转**，无底部四 Tab；uni-app 源码有 Tab 但云上演示以 HTML 为主 | 改为「首页菜单网格 + 多页面跳转」，或注明「uni-app 版含 Tab，答辩 H5 为网格入口」 |
| 测试 4.3 BUG-TC-004 | 后端用 **Redis 分布式锁** 实现放款幂等 | ContractServiceImpl.disburseContract() 通过 **existsByContractAndDisburseStatus 数据库校验** 拦截重复放款，**未使用 Redis 锁** | 改为「数据库唯一性校验 + 业务异常提示」 |
| 测试 4.3 BUG-TC-004 | 重复放款导致 **还款计划翻倍** | 放款接口 **不生成** 还款计划；计划在 **审批通过** 时 ensureRepayPlansForApplication() 生成；重复放款会抛「该合同已放款」 | 修改缺陷描述，或改为「重复放款记录/状态异常」 |
| 测试 2.1 | 用户端为 **uni-app 借款端（Android）** | 云服务器 deploy 默认挂载 **apps/uni-client/*.html H5**；uni-app 在 apps/uni-client/src 存在但非唯一交付形态 | 写清「H5 为主、uni-app 为扩展」 |
| 参考文献 | 线性时频分析、小波导引 | 与贷款风控 **完全无关** | 换为 Spring Boot、软件测试、个人信息保护相关文献 |

---

# 第一章 针对复杂工程问题的方案设计与实现

**后端负责人：[请填写姓名]**

互联网个人贷款风控系统的后端承担用户认证、贷款申请与审批、多数据源风控融合、合同签署与放款、还款计划管理、统计分析及云端部署等职责。与简单的增删改查接口不同，本系统后端需在一套可运行的工程框架内，同时解决外部数据接入、风控可解释性、业务状态一致性、多端联调部署及分层测试支撑等复杂工程问题。

本章在综设 I 需求分析与数据库设计的基础上，归纳后端开发过程中面对的 **六个核心复杂工程问题**（编号 P1～P6），并在 **1.1 方案设计、1.2 推理分析、1.3 方案实现** 三节中分别论述。三节内容按同一问题编号对应展开：1.1 说明「拟采用何种方案」，1.2 说明「为何选用该方案」，1.3 说明「方案在代码与部署中如何落地」。

---

## 1.1 针对复杂工程问题的方案设计

### P1：多数据源异构信息的融合接入

**问题描述**

本课题全称强调「多数据源融合」，任务书同时要求完成数据爬取模块开发。真实个人贷款风控除依赖系统内用户信用分外，还需引入征信报告中的逾期记录、查询次数，以及运营商侧的在网时长、实名状态等外部特征。然而在课程实验环境中，无法申请并对接人行征信、运营商实名认证等生产级接口；若将外部字段以常量或 if-else 方式硬编码在业务 Service 中，虽可短期跑通演示，但既不符合任务书对「爬取—融合」链路的要求，也无法在后续替换真实 API 时保持评分卡与规则链逻辑稳定。因此，本问题实质是：在受限环境下，如何设计一套结构清晰、可演示、可扩展的多源数据接入方案。

**方案设计**

本系统采用「**爬取解析 → 缓存落库 → 适配器读取 → 特征快照统一输出**」的四层结构：

第一，在 modules/crawler 包中实现 DataCrawlerService，使用 Jsoup 解析 resources/crawler/demo-credit.html（征信演示页）与 demo-telecom.html（运营商演示页）中的 HTML 表格。解析字段包括身份证末位 id_suffix、逾期次数 overdue_count、30 天内查询次数 query_count_30d、手机末位 phone_suffix、在网月数 online_months、是否实名 real_name_verified 等，按末位 0～9 共 10 组写入数据库表 t_external_data_cache，缓存键名形如 SUFFIX_0～SUFFIX_9，有效期由配置项 crawler.cache-ttl-hours=24 控制。

第二，在 modules/risk/datasource 包中定义 ExternalDataSourceAdapter 接口，分别实现 MockCreditBureauAdapter 与 MockTelecomAdapter。适配器根据用户身份证号、手机号末位匹配缓存记录；若缓存尚未生成，则按末位算法返回兜底值，保证系统在未执行爬取时仍可完成风控评估。

第三，由 FeatureContextService 调用各适配器，将库内字段（信用分、申请金额）与外部字段合并为 FeatureSnapshot 对象。评分卡引擎 SimpleScoringCardEngine 与规则链 RiskRuleEvaluator 仅读取该快照，不直接依赖爬虫或 HTTP 客户端，从设计上隔离「数据获取」与「风险计算」。

第四，在调度层面提供两种触发方式：DataCrawlerScheduler 定时任务（启动约 2 分钟后首次执行，之后约每 6 小时执行一次）与管理员接口 POST /api/admin/crawler/run（答辩现场可手动刷新外部数据）。

**方案比选**

| 方案 | 主要思路 | 优点 | 不足 | 结论 |
|------|----------|------|------|------|
| A | 在评分卡或 Service 中硬编码外部字段 | 实现快 | 无法体现爬取；扩展新数据源需改核心逻辑 | 不采用 |
| B | 适配器 + 特征快照 + 爬取缓存表 | 链路完整；可替换 Adapter；便于单测 | 需设计 TTL 与兜底策略 | **采用** |
| C | 各数据源独立微服务 | 解耦彻底 | 课程周期内部署、联调成本高 | 不采用 |

---

### P2：可解释的风控评估与评分卡机制

**问题描述**

任务书明确要求系统实现评分卡机制。从业务合规与答辩说明双重角度考虑，风控结论不能是缺乏依据的「黑盒输出」：管理员审批时需要知道申请人得分及分项构成，拒绝时需要明确是信用分不足、评分卡未达阈值，还是触发了逾期硬性规则。与此同时，课程项目不具备训练机器学习风控模型所需的样本规模与算力条件。因此，本问题的核心是：在可解释性、任务书符合度与实现成本之间，选择合适的风控评估模型并完成与业务链路的集成。

**方案设计**

本系统采用「**评分卡量化计分 + 规则链准入控制**」的双层结构：

**评分卡层**由 SimpleScoringCardEngine 实现，对用户信用分、申请贷款金额、征信逾期次数、运营商在网月数四个维度进行分箱计分，满分 100 分，通过阈值默认 40 分（配置项 risk.scoring-card.pass-min-points）。各维度分箱规则如下：信用分 ≥700/≥600/≥500/更低分别计 50/35/20/5 分；申请金额 ≤5 万/≤20 万/更高分别计 30/20/10 分；逾期 0 次加 10 分、1～2 次不加分、大于 2 次扣 20 分；在网 ≥24 月/≥12 月/更短分别加 10/5/0 分。计分过程生成 breakdown 分项明细，便于管理端展示「每一维加（扣）多少分」。

**规则链层**通过 Spring 容器注入多个 RiskRuleEvaluator 实现类，按 @Order 注解指定顺序依次执行：BasicRiskRuleEvaluator 校验用户信用分是否低于 450、账户状态是否正常；ScoringCardRiskEvaluator 将评分卡总分与通过阈值比较；ExternalDataRiskEvaluator 对征信逾期次数大于 2 次的情形实施硬性拒绝（阈值 datasource.risk-rule.max-overdue-count=2）。任一评估器返回不通过，则整体评估结果为拒绝。

**持久化与对外展示**：评估完成后，结果写入实体表 t_risk_report（对应实体 RiskReport），主要字段包括 riskScore、riskLevel、scoringCardPoints、scoringCardMax、cardVersion、passed、rejectReason 等。贷款列表 DTO 关联查询该表，向管理端输出 riskPassed、scoringCardPoints、rejectReason，向用户端借款记录页输出风控结论与中文状态说明。

**方案比选**

| 方案 | 可解释性 | 对样本量要求 | 与任务书匹配度 | 结论 |
|------|----------|--------------|--------------|------|
| 纯 IF-ELSE 规则 | 中等 | 无 | 中等 | 不采用 |
| 评分卡 + 规则链 | 高 | 无 | 高 | **采用** |
| 机器学习模型 | 低 | 高 | 低 | 暂不采用 |

---

### P3：风控评估触发时机与各端数据一致性

**问题描述**

风控评估若与贷款申请提交环节脱节，将引发一系列联调与演示问题：管理端贷款列表长期显示「风险未评估」，管理员无法依据评分与拒绝原因作出审批判断；用户端借款记录页无法同步展示风控结果；测试同学在设计 RC 用例时也难以固定「爬取前后分数变化」的对比基准。此外，系统启动时通过 DataInitializer 注入的演示种子数据，若未经过完整提交流程，也会缺少对应的风控报告记录。本问题的实质是：应将风控评估绑定在哪一业务事件上，以及如何保证历史数据与各端展示口径一致。

**方案设计**

本系统确定「**用户提交申请即触发风控评估**」为主路径，具体设计如下：

其一，用户通过 POST /api/loan-applications/submit 提交贷款申请时，LoanApplicationServiceImpl.submitApplication() 在将申请状态设为 pending 并持久化后，于同一业务流程内调用 RiskService.performRiskAssessment(applicationId)，将评估报告写入 t_risk_report。提交接口的返回体中同步附带 riskAssessment、riskPassed 字段，用户端可在提交后立即获知初步风控结论。

其二，列表与详情接口在 DTO 转换阶段关联查询风控报告，保证管理端、用户端读取的是同一份持久化结果，而非各自独立计算。

其三，针对演示种子数据，在应用启动类 DataInitializer 中增加 backfillRiskReports() 方法：启动时先执行 dataCrawlerService.runFullCrawl() 刷新外部缓存，再对缺少报告的贷款申请逐条补跑 performRiskAssessment()，避免管理端出现大量历史「未评估」记录。

其四，提供 GET /api/risk/applications/{id}/assessment 供查询单次评估结果；提供 POST /api/risk/applications/{id}/assess 供外部数据更新后手动重算，支撑测试用例 RC-06「爬取前后评分变化」的验证。

---

### P4：贷款全链路业务状态与数据一致性

**问题描述**

互联网个人贷款业务链条较长，涉及申请、风控、审批、合同生成与签署、放款、还款计划执行及结清等环节。各环节的先后次序若缺少约束，将产生明显的数据一致性问题。本项目在联调阶段曾出现以下典型现象：用户在申请尚未审批通过时，还款页已出现待还计划；管理员重复点击放款按钮时，系统未给出明确拦截；控制台「最近贷款」模块显示的状态与贷款管理列表不一致；用户端借款记录直接展示英文状态值 disbursed。上述问题表明，仅有功能接口的堆叠不足以保证业务正确性，必须对状态流转、关键副作用触发时机及查询口径作出显式设计。

**方案设计**

本系统以 **显式状态机 + 关键节点触发副作用 + 查询层二次过滤** 作为总体思路：

**状态流转设计**：贷款申请初始状态为 pending；管理员审批通过后变为 approved，拒绝后为 rejected；合同签署、放款完成后贷款进入 disbursed；全部还清后为 SETTLED。合同模块内部另维护合同状态，放款操作仅在合同已签署且贷款已审批的前提下执行。

**还款计划生成时机**：还款计划由 ensureRepayPlansForApplication() 生成，且**仅在** approveApplication() 审批通过流程中调用，支持等额本息、等额本金两种还款方式，按贷款期数生成多期 RepayPlan 记录。提交申请阶段不再生成计划，从业务语义上保证「未批准贷款不产生账务义务」。

**查询层过滤**：RepayPlanRepository.findPendingPlansByUserId 的 JPQL 查询增加贷款状态条件，仅关联状态为 approved、disbursed、settled、paid 的贷款申请，使用户端 /api/repayment/plans/user 接口不会返回未审批贷款的计划。

**放款幂等控制**：ContractServiceImpl.disburseContract() 在写入放款记录前，调用 disburseRecordRepository.existsByContractAndDisburseStatus() 检查该合同是否已有成功放款记录；若已存在，则抛出「该合同已放款」业务异常，阻止重复入账。需说明的是，还款计划在审批阶段已生成，放款接口本身不再创建计划，因此重复放款不会导致计划条数翻倍。

**状态展示统一**：在 LoanApplicationDTO 中增加 statusText 字段，将 pending、approved、disbursed、rejected 等内部枚举映射为「待审批」「已通过」「已放款」「已拒绝」等中文文案，减轻前端硬编码负担。

**事务边界**：审批、拒绝、合同签署、放款、还款等写操作均标注 @Transactional，任一步骤异常时整体回滚，避免中间态数据残留。

---

### P5：前后端分离条件下的多端协同与云端部署

**问题描述**

本系统包含用户 H5（apps/uni-client/）、管理端 Vue（frontend/）与 Spring Boot 后端（backend/）三个独立工程，开发阶段分别运行在不同端口。若部署时仍保持端口分离，移动端须在页面中硬编码后端 IP 与端口，并处理浏览器跨域限制；课程答辩又要求系统在云服务器上可一键启动、公网可访问。与此同时，团队规模与项目周期不支持引入微服务、注册中心、配置中心等过重基础设施。本问题因而包含两个层面：开发架构上如何实现职责分离，部署架构上如何实现三端统一访问与低成本交付。

**方案设计**

**开发架构**采用前后端分离：三个工程独立构建与版本管理，接口规范统一维护于 SpringDoc 文档（/swagger-ui.html）；前端通过 fetch 以 JSON 格式调用 REST API，后端不承担页面渲染，仅返回业务数据。

**运行架构**采用单体 Spring Boot 应用（单 JAR 部署），按业务域划分包结构，主要包括 modules/user（用户与认证）、modules/loanapplication（贷款申请）、modules/risk（风控与评分卡）、modules/crawler（数据爬取）、modules/repay（还款与逾期）、modules/statistics（控制台统计）、modules/product（产品管理）等。表现层 Controller 负责参数校验与 HTTP 响应封装，业务层 Service 承载状态机与计分逻辑，持久层 Repository 通过 JPA 访问 MySQL。

**部署架构**采用 Docker Compose 编排四个容器：mysql（业务库 zongshe）、redis（验证码等短生命周期数据）、backend（Spring Boot 服务，容器内监听 8080 端口）、web（Nginx，对外监听 80 端口）。Nginx 按路径分流：/ 托管用户 H5 静态页面，/admin/ 托管管理端 Vue 构建产物，/api/ 反向代理至 backend 容器。浏览器侧三端共用同一 IP 与端口，前端以相对路径 /api/... 发起请求，无需额外配置 CORS。

**安全与配置**：采用 JWT 无状态认证（SecurityConfig + JwtAuthenticationFilter），用户端与管理端分别登录，请求头携带 Authorization: Bearer <token>；数据库密码、JWT 密钥、管理员密码等敏感项通过 deploy/.env 注入，不写入代码仓库。Redis 承担短信验证码存储，与 MySQL 业务主库职责分离。

---

### P6：管理端统计指标口径与数据准确性

**问题描述**

管理端控制台需为管理员提供贷款申请趋势、状态分布、月度还款金额等可视化数据，数据来源于后端统计接口 /api/statistics/dashboard 与 /api/statistics/charts。若统计口径与真实业务行为不一致，图表将失去决策参考价值。联调过程中曾出现两类问题：一是月度还款柱图按应还日期 dueDate 归集，导致用户提前还款的金额计入错误月份；二是演示种子数据包含未来期次的还款计划，图表中出现当前日期尚未到达月份的柱形，且「最近贷款」模块对 disbursed 状态未作归一化处理，与贷款管理列表展示不一致。本问题的核心是：统计模块应遵循何种业务口径，如何保证控制台数据与业务库状态一致。

**方案设计**

本系统对控制台统计作如下设计：

**月度还款统计**优先依据 repayment_records 表中的 repay_time（实际还款时间）按月累加 amount 字段，采用收付实现制口径，使用户提前还款计入实际还款所在月份。对尚无流水、仅还款计划状态为 PAID 的演示数据，方以 RepayPlan.dueDate 作为兜底，且月份不得超过当前年月。

**未来月份过滤**在 StatisticsServiceImpl 中增加 isCountableRepayMonth() 判断，排除当年尚未到来的月份，避免演示数据造成虚假柱形。

**最近贷款状态归一化**在 getRecentLoans() 中通过 normalizeLoanStatus() 统一处理 disbursed、approved 等状态值，与贷款管理模块列表口径保持一致。

**前后端协同**统计接口返回 barChartMonthCount 字段，管理端 Dashboard.vue 据此限制 ECharts 柱图横轴仅展示当年 1 月至当前月，避免前端自行猜测展示范围导致与后端不一致。

---

## 1.2 针对复杂工程问题的推理分析

### P1：多数据源异构信息的融合接入

**推理过程**

多源融合方案的核心矛盾在于：外部数据「必须有来源」，而课程环境「无法接真接口」。对此，项目组首先评估在 SimpleScoringCardEngine 内直接写死逾期次数、在网月数等字段的方案。该方案实现成本最低，但存在三方面缺陷：其一，任务书明确要求数据爬取开发，硬编码无法体现爬取环节；其二，与课题名称「多数据源融合」不符，答辩时难以说明「融合」的工程含义；其三，后续对接真实征信 HTTP 接口时，必须改动评分卡核心代码，违反开闭原则。

相较之下，「爬虫解析演示 HTML → 写入 t_external_data_cache → 适配器按身份证/手机末位读取 → 汇总为 FeatureSnapshot」的方案，在课程条件下完整仿真了生产环境「外部数据更新—风控重新读取—评估结果变化」的链路。缓存 TTL 设为 24 小时，是在「演示数据可刷新」与「运行时减少重复解析」之间的折中；适配器层的末位兜底算法，则保证首次部署、尚未执行爬取时系统仍可完成端到端演示。

**推理结论**：选用适配器 + 特征快照 + 爬取缓存方案；该设计属于生产级多源接入架构的简化实现，真实上线时仅需新增实现 ExternalDataSourceAdapter 的 HTTP/SDK 适配类，评分卡与规则链无需修改。

---

### P2：可解释的风控评估与评分卡机制

**推理过程**

风控模型选型需同时回答两个问题：评估结果能否向管理员解释，以及是否符合任务书对评分卡的硬性要求。

若仅采用规则链，可以清晰表达「信用分低于 450 拒绝」「逾期大于 2 次拒绝」等条件，但难以向非技术人员呈现「80/100 分」一类直观量化结果，不利于管理端风控列的信息密度与答辩演示效果。若仅采用评分卡，则「逾期大于 2 次一律拒绝」等合规底线难以用单一阈值完整表达——例如申请人可能在扣分后总分仍高于 40，但已不符合放贷合规要求。机器学习方案虽在工业界应用广泛，但本项目无标注样本集，模型训练、特征工程与可解释性说明均超出课程范围，且与任务书「评分卡机制」表述不符。

**推理结论**：采用评分卡负责量化计分与分项展示，规则链负责准入门槛与硬性拒绝，二者结果汇总至同一 RiskReport 实体；该结构兼顾可解释性、合规表达与实现成本，与 RC 专项测试中「分数变化 + 规则触发」的验证方式相衔接。

---

### P3：风控评估触发时机与各端数据一致性

**推理过程**

风控触发时机曾有两种候选方案。**方案 A** 为管理员打开审批页或点击「评估」按钮时再执行风控——该方案将风控时序绑定于人工操作，在演示环境下常见列表长期显示「未评估」，且用户端提交后无法立即看到风控反馈。**方案 B** 为用户提交申请时同步执行评估并持久化报告——风控成为申请提交的必然后续步骤，管理端、用户端、统计模块均读取 t_risk_report 中的同一份结果，时序清晰、数据口径统一。

从软件工程的事件驱动视角分析，「提交贷款申请」是触发贷前风控评估的自然业务事件；将评估推迟至管理员操作，反而增加状态不一致窗口期。对于启动时注入、未经过 submit 接口的历史演示数据，单纯采用方案 B 仍可能遗留缺失报告的记录，故补充 backfillRiskReports() 作为启动阶段的数据修复手段，而不改变「正常运行时以提交触发为主」的总体策略。

**推理结论**：选用方案 B，并在 DataInitializer 中增加历史报告补全逻辑；同时保留 assess 重算接口，以支撑外部缓存更新后的再评估场景（RC-06）。

---

### P4：贷款全链路业务状态与数据一致性

**推理过程**

**关于还款计划生成时机**：早期实现曾在 submitApplication() 中同步生成还款计划，初衷是便于用户端借款页展示预估月供。联调测试表明，该做法使用户端还款页 /api/repayment/plans/user 返回了状态仍为 pending 的贷款所关联计划，形成「未审批即有账单」的业务矛盾。比较两种方案后认为，**业务状态机的语义正确性应优先于界面展示便利**——用户可在借款试算页通过前端计算预览月供，无需依赖后端提前生成账务计划。因此将计划生成时机调整至 approveApplication()，并在 Repository 查询层增加贷款状态过滤，形成「写入时机控制 + 读取口径过滤」的双重保障。

**关于放款幂等**：重复点击放款在管理端操作中真实存在。可选方案包括 Redis 分布式锁与数据库存在性校验。本系统并发规模有限，放款记录本身持久化于 disburse_records 表，通过 existsByContractAndDisburseStatus() 判断即可在事务内完成幂等拦截，无需引入额外分布式组件。测试报告若表述为「Redis 分布式锁」，与当前代码实现不符，应以数据库校验为准。

**关于事务边界**：审批通过同时涉及贷款状态变更、合同生成、还款计划生成等多个写操作，任一步骤失败均可能导致数据半成品。推理上应将这些操作置于同一 @Transactional 边界内，或按业务可补偿性拆分事务；本项目采用 Service 层 @Transactional 保证主流程原子性。

**推理结论**：审批通过后生成还款计划；查询层过滤未审批贷款；放款前数据库幂等校验；DTO 统一中文状态映射；关键写操作事务化。

---

### P5：前后端分离条件下的多端协同与云端部署

**推理过程**

「前后端分离」与「Nginx 统一入口」分属不同层面，并不矛盾：前者指开发时的职责与工程划分，后者指部署时的访问聚合与跨域消除。本地开发阶段，管理端 Vite 开发服务器（5173 端口）通过代理转发 /api 至后端 8080 端口；云部署阶段，Nginx 在 80 端口统一对外，三端共用 http://服务器IP，移动端 H5 的 api-config.js 以 location.origin 作为 API 基址，自动适配部署环境。

架构规模方面，微服务方案虽具备服务独立扩展优势，但在本课程周期内将引入服务注册、网关路由、分布式事务等额外问题，与「可演示、可交付」目标不符。单体 Spring Boot 配合 modules/* 分包，可在单进程内保持业务域边界清晰，又便于 Docker Compose 四容器一键启动，利于全组联调与答辩现场部署。

**推理结论**：开发上坚持前后端分离与单体模块化分包；部署上采用 Docker Compose + Nginx 同源反代；敏感配置外置 .env 文件。

---

### P6：管理端统计指标口径与数据准确性

**推理过程**

统计指标应回答「系统中实际发生了什么」，而非「计划中将要发生什么」。若按月还款计划的应还日 dueDate 归集，用户 6 月提前偿还 8 月期次款项时，金额将被错误计入 8 月，控制台月度还款曲线无法反映真实资金回流节奏。改为按 repayment_records.repay_time 归集后，统计口径与财务上的收付实现制一致，也与用户实际操作行为一致。

演示种子数据为覆盖多种场景，可能预置未来期次计划。若统计逻辑不加月份上限过滤，ECharts 柱图将在当前月为 6 月时仍显示 11 月、12 月数据，造成「系统已发生未来还款」的误解。推理上应在 Service 层过滤 YearMonth 大于当前月的记录，并将有效月份数通过 barChartMonthCount 告知前端，使前后端展示范围一致。

**推理结论**：月度还款按实还时间统计；过滤未来月份；最近贷款状态归一化处理。

---

### 面向测试分层的补充推理

除上述六个问题外，后端设计还需考虑与测试分工的衔接。Service 层集中状态机与计分逻辑，适合岳炜杰负责的 JUnit5 + Mockito 单元测试（UT001～UT013）；风控与多源融合适合熊梓伊负责的 RC 专项用例（如 RC-06）；依赖三端界面与数据库联动的场景适合 TC01～TC18 手工测试。若在 Controller 层编写业务分支，将导致单测必须启动 Web 容器或重复测试 HTTP 层，降低测试效率。因此后端在分层设计上刻意保持 Controller 轻薄、Service 可注入 Mock Repository，这一安排本身亦属于面向可测试性的工程推理。

---

## 1.3 针对复杂工程问题的方案实现

本节依据 1.2 节的推理结论，说明各项方案在代码、配置、接口及部署环境中的具体落地情况。后端整体技术栈为 Java 21、Spring Boot 3.2.5、Spring Data JPA、Spring Security、MySQL 8.0、Redis 7、Jsoup 1.17；云部署环境为阿里云 ECS（示例 IP：47.109.202.165），部署目录 /opt/zongshe/deploy。

---

### P1：多数据源异构信息融合的实现

**实现概述**

多源融合链路已在 modules/crawler 与 modules/risk/datasource 中完整实现，数据流为：demo-credit.html / demo-telecom.html → DataCrawlerService → t_external_data_cache → MockCreditBureauAdapter / MockTelecomAdapter → FeatureContextService → FeatureSnapshot。

**爬取与落库**

DataCrawlerService.runFullCrawl() 读取配置项 crawler.credit-bureau.url、crawler.telecom.url（默认 classpath 路径，亦支持 HTTP URL），使用 Jsoup 解析表格行。征信页解析字段写入键名 SUFFIX_{末位}，例如末位 9 的用户对应逾期次数 3 次；运营商页解析在网月数、实名状态等字段。爬取完成后返回摘要 { creditRows: 10, telecomRows: 10, message: "..." }。

调度方面，DataCrawlerScheduler 按 crawler.scheduled.initial-delay-ms=120000、crawler.scheduled.fixed-delay-ms=21600000 执行定时爬取；AdminCrawlerController 暴露 POST /api/admin/crawler/run，需管理员 JWT 鉴权，供答辩现场手动触发。

**适配器与特征快照**

MockCreditBureauAdapter 根据用户身份证号末位查询缓存，提取 overdue_count、query_count_30d 等字段；MockTelecomAdapter 根据手机号末位提取 online_months、real_name_verified 等字段。无缓存时按末位返回预设兜底值。FeatureContextService.build() 将上述结果与库内 credit_score、loan_amount 合并为 FeatureSnapshot，供后续模块统一读取。

**验证与测试**

- 单元测试：DataCrawlerServiceTest（UT013）验证解析与落库逻辑；
- 专项测试：RC-06 对身份证末位 9 的用户，在爬取前后分别调用 POST /api/risk/applications/{id}/assess，使用 Navicat 对比 t_external_data_cache 与 t_risk_report 中 scoringCardPoints、passed 字段变化。

---

### P2：可解释风控评估与评分卡机制的实现

**实现概述**

风控评估入口为 RiskServiceImpl.performRiskAssessment(Long applicationId)。方法内部依次完成：加载贷款申请与用户 → FeatureContextService.build() 构建特征快照 → 按 @Order 执行规则链 → 调用 SimpleScoringCardEngine.evaluate() 获取评分卡结果 → 持久化 RiskReport。

**评分卡计分实现**

SimpleScoringCardEngine 四维度计分规则已在代码中硬编码实现（版本号 risk.scoring-card.version=demo-v1），计分上限 MAX_POINTS=100，并通过 breakdown 列表记录每个维度的 featureCode、rawValue、binLabel、points。外部特征维度（逾期、在网月数）仅在 FeatureSnapshot 就绪时参与计分，与 P1 融合链路相衔接。

**规则链实现**

BasicRiskRuleEvaluator 检查 app.system.min-credit-score-for-loan=450 及用户 userStatus；ScoringCardRiskEvaluator 比较总分与 risk.scoring-card.pass-min-points=40；ExternalDataRiskEvaluator 读取快照中 credit_overdue_count，与 datasource.risk-rule.max-overdue-count=2 比较，超出则硬性拒绝。拒绝原因写入 rejectReason，如「征信逾期次数超过阈值」。

**对外接口与展示**

- GET /api/risk/applications/{id}/assessment：查询评估详情；
- 管理端贷款列表：展示 riskPassed、scoringCardPoints、rejectReason；
- 用户端 loan-records.html：展示风控通过与进度信息。

**验证与测试**

SimpleScoringCardEngineTest、ScoringCardRiskEvaluatorTest 等覆盖计分边界；mvn test 相关用例全部通过。

---

### P3：风控评估触发时机与数据一致性的实现

**实现概述**

正常运行路径下，风控评估与用户提交申请同步完成；历史数据通过启动补全；外部数据更新后可通过 assess 接口重算。

**提交即评估**

LoanApplicationServiceImpl.submitApplication() 在步骤 6 保存 pending 状态申请后，步骤 7 调用 riskService.performRiskAssessment(application.getId())。若评估过程异常，记录错误日志但不阻断申请提交成功响应，避免外部数据暂时不可用导致用户无法申贷。成功时返回体包含 riskAssessment、riskPassed 字段。

**DTO 关联查询**

convertToDTO() 方法通过 RiskReportRepository 查询最新报告，填充 DTO 中风控相关字段，保证列表接口一次请求即可返回贷款与风控信息。

**启动补全**

DataInitializer 在应用启动完成后调用 dataCrawlerService.runFullCrawl() 与 backfillRiskReports()，对演示环境中缺少报告的 pending/approved 申请逐条补评估，解决管理端「风险未评估」历史问题。

**手动重算**

RiskController 提供 POST /api/risk/applications/{id}/assess，内部再次调用 performRiskAssessment()，用于 RC-06 及答辩演示「爬取后评分变化」场景。

---

### P4：贷款全链路业务状态与数据一致性的实现

**实现概述**

贷款主流程涉及 LoanApplicationServiceImpl（申请与审批）、ContractServiceImpl（合同与放款）、RepayPlanRepository（计划查询）等类，通过状态字段、副作用触发时机与 JPQL 过滤共同保证一致性。

**审批通过后生成计划**

approveApplication() 将状态更新为 approved 后，依次调用 contractService.createContractForApplication() 与 ensureRepayPlansForApplication()。后者检查该申请是否已有计划，若无则按 @Transactional 方法 createRepayPlans() 生成多期记录，计划编号、应还日、应还本息等字段依贷款金额、期数、利率及还款方式计算。

**查询层状态过滤**

RepayPlanRepository.findPendingPlansByUserId JPQL 明确限制 loanApplication.status 属于已审批及后续状态集合，从读取侧杜绝未审批贷款计划泄漏至用户端。

**放款幂等**

ContractServiceImpl.disburseContract() 核心校验逻辑如下：若 disburseRecordRepository.existsByContractAndDisburseStatus(contract, DISBURSE_SUCCESS) 为真，则抛出 BusinessException("该合同已放款")；否则写入放款记录并更新贷款状态为 disbursed。该实现与 UT001～UT006 中重复放款测试用例一致。

**状态中文映射**

LoanApplicationDTO.statusText 将内部枚举映射为中文，用户端借款记录页不再直接显示 disbursed 等英文常量。

**单元测试**

- UT007～UT008：验证审批通过触发生成合同与计划、审批拒绝不生成合同；
- UT001～UT006：验证合同生成、签署权限、放款前状态、重复放款拦截等。

---

### P5：前后端分离与云端部署的实现

**实现概述**

后端工程位于 backend/，部署配置位于 deploy/，通过 Docker Compose 在云服务器一键启动。

**代码分层与模块划分**

主要 Controller 前缀包括 /api/users、/api/loan-applications、/api/risk、/api/repayment、/api/admin/*、/api/statistics 等。Service 层使用 @RequiredArgsConstructor 构造器注入，便于单测 Mock。合同相关服务位于 service/impl/ContractServiceImpl.java，与 modules 包内领域模块协同工作。

**安全认证实现**

SecurityConfig 配置 SecurityFilterChain：登录、注册、发送验证码、Swagger 文档等路径 permitAll()；/api/** 需 JWT 认证。JwtAuthenticationFilter 从请求头 Authorization: Bearer 解析 Token。管理员密码读取环境变量 ADMIN_PASSWORD（示例账号 yunizai）。

**Docker Compose 部署**

deploy/docker-compose.yml 定义四服务及健康检查；deploy/nginx/nginx.conf 配置静态资源与 API 反代，包括 /api/、/swagger-ui.html、/api-docs 等路径。环境变量 DB_PASSWORD、JWT_SECRET、ADMIN_PASSWORD 由 deploy/.env 提供。修改 .env 后须执行 docker compose up -d --force-recreate backend 方可使 backend 容器加载新值。

**构建优化**

云主机内存有限，容器内 Maven 构建可能失败。项目提供 backend/settings.xml（阿里云镜像）与 Dockerfile.prebuilt（本机 mvn package 后仅拷贝 JAR），相关说明见 deploy/README.md。

**访问地址（云部署）**

- 用户 H5：http://47.109.202.165/
- 管理端：http://47.109.202.165/admin/
- API 基址：http://47.109.202.165/api/
- Swagger：http://47.109.202.165/swagger-ui.html

---

### P6：管理端统计指标口径的实现

**实现概述**

统计逻辑集中于 StatisticsServiceImpl，对外由 StatisticsController 提供 /api/statistics/dashboard、/api/statistics/charts、/api/statistics/recent-loans 等接口，管理端 Dashboard.vue 通过 fetch 调用并驱动 ECharts 图表。

**按实还时间归集**

getChartData() 生成 barChartData 时，优先遍历 repayment_records 表，以 record.getRepayTime() 的月份为桶累加 amount。仅当不存在对应流水、而演示计划中某期 RepayPlan 状态为 PAID 时，才回退使用 dueDate，且通过 isCountableRepayMonth() 限制不超过当前年月。

**最近贷款与状态归一化**

getRecentLoans() 调用 normalizeLoanStatus()，将 disbursed 等状态转换为与贷款管理列表一致的展示值，修复控制台「最近贷款」与管理端贷款页状态不同步问题。

**前后端协同**

接口响应中包含 barChartMonthCount，管理端据此设置 ECharts 横轴类目数量，仅展示 1 月至当前月，与后端过滤逻辑保持一致。

---

### 实现完成度与测试支撑

| 问题编号 | 主要交付物 | 完成度 | 说明 |
|----------|------------|--------|------|
| P1 多源融合 | 爬虫、缓存表、适配器、定时/手动触发 | 85% | 演示链路完整，真 API 替换点已预留 |
| P2 评分卡与规则链 | 四维度计分、规则链、RiskReport | 85% | 分项 breakdown 可展示 |
| P3 风控触发时机 | 提交即评估、补全、assess 重算 | 90% | 管理端「未评估」问题已修复 |
| P4 状态与一致性 | 审批后计划、JPQL 过滤、放款幂等 | 88% | 联调问题已闭环 |
| P5 部署与架构 | Compose 四容器、Nginx、JWT | 80% | 云构建文档化，Swagger 反代已配置 |
| P6 统计口径 | 实还日归集、未来月过滤 | 82% | 控制台与管理端数据一致 |
| **整体** | — | **约 85%** | 可支撑答辩全链路演示 |

**测试支撑情况**：后端共 13 条单元测试（UT001～UT013），执行 mvn test 全部通过；RC、TC 用例覆盖登录、申贷、爬取重算、审批放款还款等主流程。Service 层采用构造器注入与 Mock Repository 隔离，与 1.2 节面向可测试性的推理相一致。

**延伸实现说明**：在上述六个问题解决方案之上，系统另实现合同签署状态流转、RepaymentOverdueScheduler 每日逾期扫描（repay.overdue.cron=0 0 1 * * ?）、RepaymentServiceImpl 全额/部分还款入账、ProductController 贷款产品维护等功能。这些模块均遵循 P4 状态机与 P5 分层架构约束，属于业务流程的自然延伸，本文不再逐一展开。

---

# 项目完成情况与后续改进计划

**后端负责人：[请填写姓名]**

通过本阶段的开发，我负责的后端模块已基本完成个人贷款风控系统核心业务 API、多数据源融合与评分卡风控、合同放款与还款账务、统计分析及 Docker 云部署等工作，并配合管理端、用户端与测试同学完成联调与缺陷修复。项目后期，我将系统部署至阿里云 ECS（公网 IP：47.109.202.165，目录 /opt/zongshe/deploy），通过 Nginx 实现用户 H5、管理端与 /api/ 接口同源访问，手机浏览器可直接打开用户端完成演示，支撑「注册登录 → 提交申请 → 自动风控 → 管理员审批 → 合同签署 → 放款 → 还款 → 控制台统计」的完整答辩链路。

## （一）项目完成情况

从完成度来看，后端目前整体完成度约为 **85%～88%**。其中，用户与管理员认证（JWT、注册登录、权限拦截）、贷款申请与审批、提交即风控与报告落库、合同生成/签署/放款及放款幂等校验等核心链路完成度较高（约 **88%～90%**）；评分卡与规则链、多数据源适配器与 Jsoup 爬取缓存、还款计划生成与逾期定时任务、统计接口与 statusText 中文映射等模块已完成主要功能，细节与工程化仍有优化空间（约 **82%～85%**）；Docker Compose 四容器编排、Nginx 反代、部署文档与云上联调排错已落地，可稳定支撑答辩演示（约 **85%**）。单元测试方面，Service 层共编写 **13 条用例（UT001～UT013）**，本地执行 mvn test 全部通过；RC、TC 功能测试用例已设计并部分在云环境执行，全链路自动化覆盖仍待加强（约 **70%**）。

在业务模块方面，认证与用户模块（/api/users、/api/admin/login）支持密码与验证码登录、资料维护；贷款申请模块（/api/loan-applications）支持提交、审批、列表及 statusText 输出；风控模块（/api/risk、/api/credit）实现评分卡计分、规则链准入、t_risk_report 持久化及信用评估接口；爬虫与融合模块实现演示 HTML 解析、t_external_data_cache 缓存落库与特征快照输出，并提供定时任务与 POST /api/admin/crawler/run 手动触发；合同与放款模块实现审批后生成合同、签署、放款及数据库幂等拦截重复放款；还款模块实现审批后生成计划、全额/部分还款入账及 RepaymentOverdueScheduler 逾期扫描；统计模块提供控制台、图表与最近贷款接口，月度还款按实还日归集并过滤未来月份。上述接口与 deploy/ 目录下的 docker-compose.yml、nginx.conf、.env 等配置共同构成可答辩、可演示的后端交付物。

部署方面，我编写了 deploy/docker-compose.yml，编排 mysql、redis、backend、web 四个容器，配置健康检查与数据卷持久化，敏感项通过 .env 注入；编写 deploy/nginx/nginx.conf，按路径分流用户 H5（/）、Vue 管理端（/admin/）与后端 API（/api/），并补充 Swagger 相关反代规则以解决联调中的 404 问题；编写 deploy/web/Dockerfile 在镜像构建阶段完成管理端 npm build 并打包 H5 静态页，实现一次 compose up 交付三端；同时整理 deploy/deploy.sh、deploy/README.md 与 .env.example，记录服务器配置、手机访问方式及常见问题。云主机构建 backend 镜像时曾遇 Maven 中央仓库不可达，通过 backend/settings.xml 配置阿里云镜像缓解；内存不足时采用 Dockerfile.prebuilt 在本机预编译 JAR 后上传。配合用户端 api-config.js，云部署下手机以 location.origin 访问 /api/，无需硬编码 localhost。全组最终以 http://47.109.202.165/ 为统一联调与彩排环境。

## （二）目前仍存在的不足

目前后端仍然存在一些不足。第一，外部数据源仍以 Jsoup 解析演示 HTML + 缓存表为主，尚未对接真实征信或运营商 OpenAPI，课程环境下可演示融合链路，但与生产级数据接入仍有差距。第二，集成测试与 API 层回归覆盖不足，虽有 13 条 Service 单测，MockMvc 级别的接口集成测试尚未系统开展，部分边界场景仍依赖手工 RC/TC 验证。第三，管理端缺少「一键触发爬取」的前端入口，爬虫接口已实现，答辩演示需借助 Swagger 或 curl。第四，配置与敏感项分散在 application.properties 与 deploy/.env 两处，新成员上手需同时阅读部署文档与 Swagger。第五，云服务器容器内 Maven 构建受网络与内存影响，虽已提供镜像加速与 prebuilt 方案，CI/CD 自动化流水线尚未建立，镜像更新仍依赖手工上传与 compose build。第六，HTTPS、集中日志与监控未在 v1 范围完整实现，当前以 HTTP 80 端口与容器日志为主。第七，部分演示能力依赖 DataInitializer 种子数据与 backfillRiskReports 补全，空库迁移需按 README 顺序操作。第八，用户端银行卡绑定以前端 localStorage 演示为主，后端尚未提供完整的银行卡持久化 API。

## （三）后续改进计划

针对以上不足，后续计划从以下几个方面继续推进。首先，完善 ExternalDataSourceAdapter 扩展点，逐步引入沙箱征信接口替换 HTML 爬取。其次，补充 MockMvc 集成测试，与 UT001～UT013 形成双层回归。再次，在管理端增加爬取触发按钮，并将 deploy/README.md 中的环境变量一览与排错步骤整理为运维手册。然后，推广 Dockerfile.prebuilt 或 CI 流水线预编译 JAR，减少云主机构建失败；可选配置 Nginx HTTPS 与证书自动续期。接着，配合测试同学完成 RC/TC 全链路回归，确保双端状态、统计口径与风控展示一致。最后，在 v2 范围考虑银行卡后端持久化、操作审计、接口限流与数据脱敏等能力。

## （四）阶段小结

总体来看，本阶段后端已完成个人贷款风控系统的核心业务引擎与云端部署底座，在评分卡可解释风控、提交即评估、审批后生成还款计划、放款幂等与统计口径统一等复杂工程问题上形成了可运行的方案与代码实现，并将系统从本地联调推进至阿里云公网可演示状态。虽然在外部数据真实对接、集成测试广度、HTTPS 与自动化运维等方面仍有改进空间，但整体已能支撑三端联调、手机 H5 答辩演示与文档交付，为团队总结答辩奠定了基础。

---

# 第二章 系统测试（后端支撑说明）

> 本章主体由岳炜杰、熊梓伊撰写。以下为 **后端在测试环境中的实现与配合**，可并入「测试环境搭建」2.1 节。

**后端：**

1. **单元测试环境**：后端 Service 层不启动 Servlet 容器、不连接 MySQL 即可运行 mvn test，与测试同学 UT001–UT013 规划一致。

2. **风控/功能测试环境**：deploy/docker-compose.yml 启动后，后端暴露 http://<host>/api/，Swagger 为 http://<host>/swagger-ui.html；MySQL 库名 zongshe，表由 JPA 自动建表 + DataInitializer 种子数据。

3. **测试数据构造支持**：身份证/手机 **末位 0–9** 对应 demo-credit.html、demo-telecom.html 不同逾期与在网月数；执行爬虫后写入 t_external_data_cache，RC-06 可通过 POST /api/risk/applications/{id}/assess 触发重算。

4. **后端配合修复的典型问题**（与测试联动）：风控报告补全、还款计划审批后生成、statusText 统一、disbursed 统计归一化、月度还款按实还日统计等。

5. **说明**：测试报告中「放款 Redis 分布式锁」与代码不符，实际为 **数据库放款记录存在性校验**；若保留该 BUG 案例，建议按 1.3 节实现更正描述。

---

# 第三章 知识技能学习情况

**后端：**

**（一）框架与架构**  
通过 Spring Boot 3.2.5 与 Java 21，系统实践了 Controller–Service–Repository 分层、构造器注入、@Transactional 事务边界及按业务域分包（modules/user、loanapplication、risk、crawler、repay、statistics 等）的组织方式。理解了前后端分离下单体应用仍可通过清晰模块边界保持可维护性，以及 DTO 作为前后端契约的重要性。项目后期将后端与前端一并容器化部署，进一步体会到分层架构对「业务代码与运行环境解耦」的意义。

**（二）安全与认证**  
学习了 Spring Security 过滤链、JWT 签发与校验、无状态会话及用户端/管理端分权登录；认识到 JWT_SECRET、DB_PASSWORD 等敏感项必须外置。在编写 deploy/.env 与 docker-compose.yml 环境变量映射的过程中，实践了配置与代码分离，避免密钥写入 Git 仓库。

**（三）JPA 与数据建模**  
掌握了实体关联、JPQL 按贷款状态过滤还款计划、ddl-auto=update 在课程迭代中的利弊；通过还款计划生成时机、放款幂等、statusText 映射等联调问题，理解了状态字段与副作用触发时机对数据一致性的影响。

**（四）设计模式与风控工程**  
在风控模块中实践了适配器模式（ExternalDataSourceAdapter）、策略模式（RiskRuleEvaluator、CreditScoreCalculator）与责任链式规则执行；理解了「爬取—缓存—特征快照—评分卡/规则链」流水线的设计思想，以及可解释风控在答辩说明中的价值。

**（五）爬虫与多源数据融合**  
学习 Jsoup 解析 HTML 表格、按末位分片写入 t_external_data_cache、适配器读库与算法兜底；掌握了在无法对接生产 API 时，如何用工程化链路满足任务书「数据爬取 + 融合」要求。

**（六）容器部署与运维**  
学习 Docker Compose 编写四服务编排、depends_on 与健康检查、数据卷持久化；编写 Nginx 配置实现 /、/admin/、/api/ 路径分流及 Swagger 反代；在 deploy/web/Dockerfile 中实践 Node 构建 Vue 与 Nginx 托管静态资源的多阶段镜像流程。云部署过程中实际处理过 Maven 中央仓库不可达（settings.xml 阿里云镜像）、deploy.sh CRLF 换行、Swagger 404、修改 .env 后须 force-recreate backend 等问题，并将解决方案写入 deploy/README.md。配合用户端 api-config.js 实现手机浏览器同源访问 /api/，使系统具备公网答辩演示条件。

**（七）可测试性设计与协作测试**  
配合测试同学，学习 JUnit5 + Mockito 隔离 Repository、ArgumentCaptor 校验入库字段；理解 Service 层单测（UT001～UT013）与 RC/TC 功能测试的分工，以及后端为可测性所做的构造器注入与业务异常设计。

**文献与资料查阅：** Spring Boot Reference Documentation、OWASP JWT Cheat Sheet、Jsoup 官方文档、Docker Compose 与 Nginx 官方文档、《软件测试》（理解 RC/TC 用例设计）、《数据库系统概论》（表设计与状态机持久化）。

---

# 第四章 分工协作与交流情况

**后端：（请填写姓名）**

本人负责后端整体架构、核心业务 API、风控与爬取模块、Docker 云部署及前后端联调支持，在团队中承担后端负责人角色。

**分工内容：**  
独立完成或主导 backend/ 下用户认证、贷款申请与审批、评分卡与规则链风控、Jsoup 爬虫与多源适配器、合同签署与放款、还款计划与逾期任务、统计接口、产品与系统设置等模块；维护 application.properties 中风控阈值、爬虫 URL、定时任务等配置；编写并维护 UT001～UT013 单元测试。项目后期，将系统部署至阿里云 ECS（47.109.202.165），编写 deploy/docker-compose.yml、nginx.conf、deploy/web/Dockerfile、deploy.sh 与 deploy/README.md，完成 mysql、redis、backend、web 四容器编排及 H5/管理端/API 同源反代；处理云主机构建失败、Swagger 404、脚本换行等排错问题，配合用户端实现云下同源 API 访问。配合钟妮对接管理端接口字段（riskPassed、scoringCardPoints、statusText、barChartMonthCount 等）；配合邢芙确认用户端 H5 接口路径与 Token 传递；配合岳炜杰、熊梓伊根据 RC/TC 与单测反馈修复状态机、统计口径、风控补全等问题。

**协作方式：**  
接口以 Swagger 为契约；联调问题通过「接口 JSON 响应 + 数据库表截图」定位；后端修复逻辑与 DTO 后通知前端更新映射。nginx.conf、.env 等部署变更在组内同步，避免成员仍按 localhost 联调。答辩彩排前走通云环境完整演示脚本，确保访问地址与文档一致。

**团队交流收获：**  
认识到 DTO 字段与状态枚举是前后端协作的契约，statusText、riskPassed 等统一输出减少了双端展示不一致的返工；测试同学 UT/RC 用例帮助提前发现跨用户签署、未审批生成还款计划、重复放款等问题；云部署使全组共用同一公网环境，环境变量规范（ADMIN_PASSWORD、DB_PASSWORD、JWT_SECRET）与排错文档需全员知晓，否则易出现本地正常、云上 401/404 的协作断层；通过 Nginx 反代与 Compose 编排实践，加深了对前后端分离与同源部署架构的整体理解。

---

## 后端相关参考文献（建议替换原报告中的无关文献）

[1] Spring Team. Spring Boot Reference Documentation[EB/OL]. https://docs.spring.io/spring-boot/docs/current/reference/html/  
[2] Walls C. Spring in Action[M]. Manning Publications, 2022.  
[3] 萨师煊, 王珊. 数据库系统概论[M]. 高等教育出版社.  
[4] 朱少民. 软件测试方法和技术[M]. 清华大学出版社.  
[5] 全国人民代表大会常务委员会. 中华人民共和国个人信息保护法[EB/OL]. 2021.

---

*合并说明：将「后端：」各节插入团队报告对应 1.1 / 1.2 / 1.3 / 第三章 / 第四章位置；**「项目完成情况与后续改进计划」** 作为独立章节插入（与用户端、管理端、测试各小节并列）；第二章插入「后端支撑说明」小节；按文首表格修正其他组员明显与代码不符的表述。*
