# 综设 III · 多模型风控设计（岳炜杰主责）

**状态：** 组内定稿为 **A/B/C/F 四卡**（信贷全生命周期），不是「一张申请卡打天下」。  
**按周任务：** 见 `docs/综设III-12周开发计划.md` 第 4 节（岳 / 钟妮；C 卡催收侧薛配合）。  
**进程：** 信贷与平台同一 Spring Boot（8080）；风控后拆 8081；网关 8088。  
**契约：** `POST /api/risk/assess` → `RiskReportDTO` 继续当跨服务合同。

对外一句话：

> 多模型 = 业内 A/B/C/F 四卡：A 申请卡（不低于 II 评分卡）+ B 行为卡 + C 催收卡 + F 反欺诈卡。  
> 硬规则做准入，公示数据喂给 A/F；授信阶段 A 卡与外部分可加权；决策可回放、可开关。

全程 **Java**。各卡系数专家给定，不训练机器学习，不上 Drools。

---

## 0. 老师怎么听（先读）

II 已有**申请评分卡**。老师要求「智能风控**不低于**该评分卡」且「**多模型**」。

- **不低于：** A 卡沿用 II 分箱打分（可升级 WOE），申请侧不会比 II 弱。  
- **不是还做一张卡：** B/C/F 出现在**贷中、催收、反欺诈**三个不同时点，II 的卡覆盖不了。  
- **硬规则 / 公示不是第四、第五张卡：** 它们是准入政策和数据源，喂给 A/F，避免和四卡抢身份。

四卡在生命周期上错开，这才叫多模型，而不是申请时并排四个总分。

```
申请瞬间     F卡（反欺诈）+ 硬规则 + 失信公示  → 准入
             A卡（申请评分）+ LPR 环境         → 授信
放款之后     B卡（行为）                        → 重评 / 建议额度
逾期之后     C卡（催收）                        → 催收紧急度（L1/L2）
```

---

## 1. 四卡分别算什么

| 卡 | 业内含义 | 何时跑 | II | III 实现 | 谁写 |
|----|----------|--------|-----|----------|------|
| **A 申请卡** | Application | 申贷授信 | 已有分箱加分 | 升级为 `ApplicationScoreCard`（保留 II 维度，可 WOE）；`cardVersion` 如 `A-card-v2` | 岳 |
| **B 行为卡** | Behavior | 贷中重评 | 无 | `BehaviorScoreCard`：逾期次数、是否足额；新用户中性分；可关 | 岳 |
| **C 催收卡** | Collection | 已逾期 | 无 | `CollectionScoreCard`：逾期天数、应还金额、历史逾期 → 0～100 紧急度 | 岳算分，薛用来升 L1/L2 |
| **F 反欺诈卡** | Fraud | 申贷准入 | 无 | `FraudScoreCard`：短时重复申请、金额异常跳动；高分拒或转人工 | 岳 |

失信公示：**命中仍走硬规则硬拒**（政策，不当成 F 卡唯一依据）。命中结果写入快照，F 卡可把「命中」当特征加分，但拒因仍是 `DISHONEST_PUBLIC`。  
LPR：宏观环境，进 **A 卡旁的外部维** 或授信加权，不进 F 卡。

配置：

```
risk.card.a.enabled=true
risk.card.b.enabled=true
risk.card.c.enabled=true
risk.card.f.enabled=true
```

关掉 B 卡：重评没有行为分，A 卡结论仍在（证明申请侧不低于 II）。  
关掉 F 卡：准入只剩硬规则+失信（对照演示用，默认必须开）。

---

## 2. 两阶段里四卡怎么串

```
阶段一 准入（能不能进门）
  F 卡          高欺诈分 → REJECTED / MANUAL_REVIEW
  硬规则        信用分门槛、用户状态
  失信公示      命中 → REJECTED（DISHONEST_PUBLIC）
  通过才进入阶段二

阶段二 授信（借多少）
  A 卡          申请资质分（不低于 II）
  LPR 外部维    环境分
  （可选）B 卡  仅当已有还款历史；新客跳过或中性
  → decision + suggestLimit

阶段三 贷中（已放款，不走授信重批）
  B 卡 reevaluate → 是否关注、suggestLimit；不改借据

阶段四 贷后逾期
  C 卡 → collectionUrgency
  薛：延迟队列到期后，按 C 卡分决定 L1 还是更快升 L2（延迟仍然要等，C 卡决定力度）
```

`RiskReportDTO.breakdown` 必须能看见四卡（没有的阶段写 `null` / `skipped`）：

```json
{
  "stage": "CREDIT",
  "admission": "PASS",
  "cardA": { "score": 72, "version": "A-card-v2", "enabled": true },
  "cardF": { "score": 18, "version": "F-card-v1", "enabled": true },
  "cardB": { "score": null, "skipped": "NEW_CUSTOMER" },
  "cardC": { "score": null, "skipped": "NOT_OVERDUE" },
  "external": { "lpr": 22, "dishonestHit": false },
  "suggestLimit": 8000
}
```

Web 报告页四块卡 + 准入结论；回放只读快照。

授信融合：A 卡为主、LPR 为辅，**不要用 AHP 硬揉四张卡**（它们不同时出现）。AHP 若做，只用于「A 卡 vs LPR」两维，可砍。

---

## 3. 各卡最小特征（专家表，课设够用）

**A 卡（沿用 II，可加 WOE 皮）**  
信用分分箱、申请金额分箱、可选逾期次数。通过阈值不低于 II 的 40 分口径（可配置）。

**F 卡**  
同一 `userId`/手机号 2 分钟内重复申贷、金额相对上次异常放大、（可选）失信命中加权。高分原因码如 `FRAUD_VELOCITY`。

**B 卡**  
历史逾期次数、最近一期是否足额、当前逾期天数。无还款记录 → 中性分或 `skipped`。

**C 卡**  
逾期天数、逾期金额、是否已催未还。分高 → 薛升 L2；分低 → 维持 L1。  
C 卡**不替代**延迟队列：「先等一会儿」仍是薛的创新点，C 卡只决定等完之后催多重。

---

## 4. 对外接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/risk/assess` | 申贷：准入（F+规则+失信）+ 授信（A+LPR） |
| POST | `/api/risk/replay/{reportId}` | 只读快照 |
| POST | `/api/risk/reevaluate` | 贷中 B 卡；`suggestLimit`；不改借据 |
| GET | `/api/risk/collection-score/{applicationId}` | 逾期后 C 卡，给催收用 |
| GET | `/api/risk/features/{userId}` | 当前特征 |
| POST | `/api/admin/crawler/run` | 手动爬公示 |

---

## 5. 建议新写的类（岳）

| 类 | 卡 |
|----|----|
| `ApplicationScoreCard`（可由 II `SimpleScoringCardEngine` 升级） | A |
| `FraudScoreCard` | F |
| `BehaviorScoreCard` | B |
| `CollectionScoreCard` | C |
| `TwoStageRiskOrchestrator` | 准入/授信编排 |
| 快照读写 | 回放 |

硬规则继续 `RiskRuleEvaluator`。不必再单独一个「外部公示模型类」和四卡并列；公示适配器给准入和快照用即可。

---

## 6. 按周

| 周 | 做什么 |
|----|--------|
| 1 | 契约；breakdown 预留 `cardA/B/C/F` |
| 4 | A 卡升级 + F 卡骨架 + 两阶段准入 |
| 5 | 失信硬拒进准入；LPR 进授信；快照回放 |
| 7 | B 卡可开关；`reevaluate` |
| 8 | C 卡；薛催收读取紧急度 |
| 9 | 能演示四卡后再迁 8081 |

---

## 7. 谁改什么

- **岳：** 四卡 Java、编排、快照；不改放款还款账。  
- **钟妮：** 报告页四块卡（无则显示未触发）；回放；演示路径。  
- **熊：** 只收 `decision` / 原因码 / `suggestLimit`。  
- **薛：** 延迟催收；读 C 卡分决定 L1/L2 力度；不写四卡公式。
