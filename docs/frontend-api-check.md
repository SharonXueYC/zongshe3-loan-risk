# 管理员端真实接口扫描报告

> 扫描日期：2026-09-19  
> 扫描范围：`backend/src/main/java`  
> 用途：第三周管理员端接口联调准备。本文仅依据当前源码整理，未调用接口、未修改后端或 Gateway。

## 1. 鉴权与通用约定

### 1.1 Authorization

`SecurityConfig` 仅放行管理员登录、用户登录/注册、Swagger 和静态资源；其余 `/api/**` 均配置为 `authenticated()`。

除 `POST /api/admin/login` 外，本文列出的接口都应发送：

```http
Authorization: Bearer <JWT token>
Content-Type: application/json
```

缺少 Token、Token 格式错误或 Token 无效时，请按 401/403 处理。`JwtAuthenticationFilter` 只建立“已认证”身份，当前源码未配置角色/权限；即没有看到“必须为 admin 角色”的接口级判断。前端仍应使用管理员登录接口签发的 Token。

### 1.2 常见响应格式

绝大多数接口使用以下包装：

```json
{
  "success": true,
  "message": "可选消息",
  "data": {},
  "total": 0
}
```

失败响应通常为：

```json
{
  "success": false,
  "message": "错误信息"
}
```

全局校验/系统异常还可能包含：

```json
{
  "success": false,
  "code": "VALIDATION_ERROR | SYSTEM_ERROR | 业务错误码",
  "message": "错误信息",
  "timestamp": 0,
  "path": "/api/..."
}
```

例外：`POST /api/admin/login` 和 `POST /api/risk/assess` 直接返回 DTO，不包在 `data` 中。

## 2. 页面与接口对应关系

| 前端页面 | 建议对接接口 | 用途 |
|---|---|---|
| 管理员登录 | `POST /api/admin/login`、`GET /api/admin/check-auth`、`POST /api/admin/logout` | 登录、路由鉴权、退出 |
| 产品管理 | `GET /api/products`、`POST /api/products`、`PUT /api/products/{id}`、`DELETE /api/products/{id}`；可选 `GET /api/products/{id}` | 列表、创建、编辑、删除、详情 |
| 贷款申请列表 | `GET /api/loan-applications`；可选 `GET /api/loan-applications/statistics` | 列表筛选、统计 |
| 贷款申请详情 | `GET /api/loan-applications/{id}`；后续审批可用 `PUT .../{id}/approve|reject` | 详情、审批 |
| 风险报告 | 首选 `GET /api/risk/applications/{applicationId}/assessment`；摘要可用 `GET .../report` | 完整规则结果、特征快照、报告摘要 |
| 风险评估触发 | `POST /api/risk/applications/{applicationId}/assess` | 管理端手动重评；当前阶段不应在只读详情页自动调用 |

## 3. 数据模型

### 3.1 ProductDTO

```json
{
  "id": 1,
  "productNo": "PROD-XXXXXXXX",
  "productName": "普通贷",
  "productType": "GENERAL",
  "minAmount": 1000.00,
  "maxAmount": 100000.00,
  "minTerm": 1,
  "maxTerm": 12,
  "interestRate": 8.50,
  "productDescription": "说明",
  "status": "active",
  "createdAt": "2026-09-19T10:00:00",
  "updatedAt": "2026-09-19T10:00:00",
  "amountRange": "1000.00-100000.00元",
  "termRange": "1-12个月"
}
```

说明：Lombok/Jackson 会同时序列化计算型 getter，因此响应可能包含 `amountRange`、`termRange`。后端创建时默认 `status = "active"`；当前源码不是 `1/0`。

### 3.2 LoanApplicationDTO

```json
{
  "id": 1,
  "applicationNo": "LA-...",
  "applicantName": "张三",
  "idCardNumber": "...",
  "phoneNumber": "...",
  "loanType": "个人消费贷",
  "loanAmount": 50000.00,
  "loanTerm": 12,
  "applyTime": "2026-09-19T10:00:00",
  "status": "pending",
  "statusText": "待审核",
  "auditRemark": null,
  "auditTime": null,
  "incomeInfo": null,
  "description": "用途说明",
  "riskScore": 30,
  "riskLevel": 1,
  "riskPassed": true,
  "scoringCardPoints": 68,
  "scoringCardMax": 100,
  "rejectReason": null
}
```

实际状态值为小写：`pending`、`approved`、`rejected`、`disbursed`、`settled`。

### 3.3 完整风险评估 data

`GET .../assessment` 与 `POST .../assess` 的 `data` 来自 `RiskService`：

```json
{
  "applicationId": 1,
  "overallRiskScore": 30,
  "overallRiskLevel": 1,
  "passed": true,
  "rejectReason": null,
  "riskReportId": 10,
  "scoringCardPoints": 68,
  "scoringCardMax": 100,
  "cardVersion": "...",
  "evaluatedAt": "2026-09-19T10:00:00.000+00:00",
  "ruleResults": [
    {
      "ruleName": "规则名称",
      "passed": true,
      "reason": "规则说明",
      "riskScore": 30,
      "riskLevel": 1,
      "details": {}
    }
  ],
  "featureSnapshot": {},
  "dataSourceResults": {}
}
```

新生成报告的结果可能没有 `applicationId`；已保存报告再次查询时可能没有 `evaluatedAt`。`ruleResults`、`featureSnapshot`、`dataSourceResults` 也可能因旧数据或详情 JSON 解析失败而缺失，前端必须做空值兼容。

## 4. 产品接口（ProductController）

所有接口都需要 Bearer Token，对应“产品管理”页面。

### 4.1 获取产品列表

- **方法/路径**：`GET /api/products`
- **Query**：`type?: string`、`status?: string`、`search?: string`
- **建议值**：`type=GENERAL|CONSUME`，`status=active|inactive`
- **成功响应**：`{ success: true, data: ProductDTO[], total: number }`
- **失败响应**：HTTP 500，`{ success: false, message: "获取产品列表失败" }`
- **前端页面**：产品管理列表与筛选

注意：Service 使用 `if / else if`，同时提交 `type` 和 `status` 时只应用 `type`；`search` 会继续应用。`type/status` 的“全部”值应省略参数或传 `all`，不要传空字符串（产品 Service 会把空字符串作为真实筛选值）。前端若需组合筛选，应确认后端后续是否修正。

### 4.2 获取产品详情

- **方法/路径**：`GET /api/products/{id}`
- **Path**：`id: Long`
- **成功响应**：`{ success: true, data: ProductDTO }`
- **不存在**：HTTP 400，`{ success: false, message: "产品不存在" }`
- **异常**：HTTP 500
- **前端页面**：编辑产品时按需回查；当前列表数据足够时可不调用

### 4.3 创建产品

- **方法/路径**：`POST /api/products`
- **Body**：`ProductDTO`；创建时核心字段为 `productName`、`productType`、`minAmount`、`maxAmount`、`minTerm`、`maxTerm`、`interestRate`、`productDescription?`、`status`
- **成功响应**：`{ success: true, message: "产品创建成功", data: ProductDTO }`
- **失败响应**：HTTP 400，`{ success: false, message: "产品创建失败: ..." }`
- **前端页面**：新增产品

### 4.4 更新产品

- **方法/路径**：`PUT /api/products/{id}`
- **Path**：`id: Long`
- **Body**：完整 `ProductDTO` 编辑字段；Service 会直接覆盖所有可编辑字段，不是 PATCH
- **成功响应**：`{ success: true, message: "产品更新成功", data: ProductDTO }`
- **不存在/失败**：HTTP 400，`{ success: false, message: "产品不存在 | 产品更新失败: ..." }`
- **前端页面**：编辑产品

### 4.5 删除产品

- **方法/路径**：`DELETE /api/products/{id}`
- **Path**：`id: Long`
- **成功响应**：`{ success: true, message: "产品删除成功" }`
- **不存在**：HTTP 400；其他异常 HTTP 500
- **前端页面**：产品管理删除

## 5. 贷款申请接口（LoanApplicationController）

所有接口都需要 Bearer Token。管理员页面主要使用 5.1～5.5；5.6、5.7 是用户端接口，一并记录以避免误用。

### 5.1 获取申请列表

- **方法/路径**：`GET /api/loan-applications`
- **Query**：`status?: string`、`loanType?: string`、`search?: string`
- **状态值**：后端使用小写 `pending|approved|rejected|disbursed|settled`，`all` 或空表示全部
- **search 匹配**：申请人姓名、申请编号、手机号
- **成功响应**：`{ success: true, data: LoanApplicationDTO[], total: number }`
- **失败响应**：HTTP 500
- **前端页面**：贷款申请列表

注意：同时传 `status` 与 `loanType` 时只应用 `status`；`search` 会继续应用。当前前端 Mock 使用的 `PENDING/APPROVED/REJECTED` 必须在联调时转为后端小写值，或统一规范。

### 5.2 获取申请详情

- **方法/路径**：`GET /api/loan-applications/{id}`
- **Path**：`id: Long`（数据库主键，不是 `applicationNo`）
- **成功响应**：`{ success: true, data: LoanApplicationDTO }`
- **不存在**：HTTP 400，`{ success: false, message: "贷款申请不存在" }`
- **异常**：HTTP 500
- **前端页面**：贷款申请详情

### 5.3 审批通过

- **方法/路径**：`PUT /api/loan-applications/{id}/approve`
- **Body**：`{ "remark": "审批备注" }`
- **成功响应**：`{ success: true, message: "贷款申请已审批通过" }`
- **失败响应**：HTTP 400 或 500
- **副作用**：状态写为 `approved`，更新信誉事件，尝试自动生成合同并生成还款计划
- **前端页面**：未来贷款详情审批功能；当前风险报告页面不应调用

### 5.4 审批拒绝

- **方法/路径**：`PUT /api/loan-applications/{id}/reject`
- **Body**：`{ "remark": "拒绝原因" }`
- **成功响应**：`{ success: true, message: "贷款申请已拒绝" }`
- **失败响应**：HTTP 400 或 500
- **副作用**：状态写为 `rejected`，更新信誉事件
- **前端页面**：未来贷款详情审批功能；当前风险报告页面不应调用

### 5.5 获取申请统计

- **方法/路径**：`GET /api/loan-applications/statistics`
- **参数**：无
- **成功响应**：

```json
{
  "success": true,
  "data": {
    "total": 0,
    "approved": 0,
    "pending": 0,
    "rejected": 0,
    "totalAmount": 0,
    "approvedPercent": 0,
    "pendingPercent": 0,
    "rejectedPercent": 0,
    "byType": {},
    "trend": { "2026-09": { "approved": 0, "pending": 0, "rejected": 0 } }
  }
}
```

- **前端页面**：贷款申请列表顶部统计（可选）

### 5.6 提交贷款申请（用户端）

- **方法/路径**：`POST /api/loan-applications/submit`
- **Body**：`{ loanType, loanAmount (>=1000), loanTerm (>=1), repaymentMode?, interestRate?, description? }`
- **成功响应**：`{ success, applicationNo, applicationId, message, riskAssessment?, riskPassed? }`
- **鉴权**：Bearer 用户 Token；Controller 会从 Token 提取 userId
- **前端页面**：非管理员页面，不应由管理端调用

### 5.7 获取指定用户申请（用户端）

- **方法/路径**：`GET /api/loan-applications/user/{userId}`
- **成功响应**：`{ success: true, data: LoanApplicationDTO[], total: number }`
- **前端页面**：非管理员页面；管理端列表应使用 5.1

## 6. 风险接口

所有风险与信誉分接口都需要 Bearer Token。

### 6.1 获取完整风控评估

- **方法/路径**：`GET /api/risk/applications/{applicationId}/assessment`
- **Path**：`applicationId: Long`
- **成功响应**：`{ success: true, data: 完整风险评估 data }`
- **失败响应**：HTTP 400，`{ success: false, message }`
- **前端页面**：风险报告页面、贷款详情风险区
- **重要副作用**：没有已持久化报告时，GET 会自动执行评估并写入报告；它不是严格只读查询

### 6.2 获取风控报告摘要

- **方法/路径**：`GET /api/risk/applications/{applicationId}/report`
- **成功且有报告**：

```json
{
  "success": true,
  "data": {
    "riskReportId": 1,
    "riskScore": 30,
    "riskLevel": 1,
    "passed": true,
    "rejectReason": null,
    "scoringCardPoints": 68,
    "scoringCardMax": 100,
    "cardVersion": "..."
  }
}
```

- **无报告**：HTTP 200，`{ success: false, message: "暂无风控报告" }`
- **失败**：HTTP 400
- **前端页面**：仅需摘要的风险报告页面；该接口不会自动评估

### 6.3 手动触发风控评估

- **方法/路径**：`POST /api/risk/applications/{applicationId}/assess`
- **参数**：仅 Path `applicationId`
- **成功响应**：`{ success: true, data: 完整风险评估 data }`
- **失败响应**：HTTP 400
- **副作用**：重新计算并保存风险报告
- **前端页面**：未来“重新评估”操作；当前只读报告不调用

### 6.4 批量评估

- **方法/路径**：`POST /api/risk/applications/batch-assess`
- **Body**：`{ "applicationIds": [1, 2, 3] }`
- **成功响应**：`{ success: true, totalCount, successCount, failCount, results: [...] }`
- **空列表**：HTTP 400，`{ success: false, message: "applicationIds 不能为空" }`
- **前端页面**：未来批量风控操作，不属于当前详情展示

### 6.5 风控契约 Mock 接口

- **方法/路径**：`POST /api/risk/assess`
- **Body**：`RiskAssessmentRequest`：`applicationId`、`userId`、`applicantName`、`idCardNumber`、`phoneNumber`、`loanType`、`productType`、`loanAmount`、`loanTerm`、`usage`、`channelId`、`description`
- **响应**：直接返回 `RiskReportDTO`，不含 `success/data`

```json
{
  "reportId": 1001,
  "applicationId": 1,
  "userId": 1,
  "decision": "APPROVED | REJECTED",
  "totalScore": 68,
  "maxScore": 100,
  "cardVersion": "card-v1",
  "reasonSummary": "...",
  "reasonCodes": ["LPR_OK"],
  "breakdown": { "scorecard": 68, "rules": 12, "external": 7, "behavior": 0 },
  "featureSnapshot": { "userId": 1, "age": 22, "creditScore": 720 },
  "stale": false,
  "assessedAt": "2026-09-19T10:00:00"
}
```

- **前端页面**：不建议作为当前管理端报告数据源；源码标注为“第1周契约版固定 Mock”

### 6.6 信誉分相关（CreditController）

| 方法与路径 | 参数 | 成功 JSON | 管理端用途 |
|---|---|---|---|
| `GET /api/credit/users/{userId}/evaluation` | Path `userId: String` | `{ success, userId, creditScore, maxCreditScore: 850, creditLevel, loanLimit, qualified, strategyName, factors: [], submittedDocuments: [] }` | 用户信息/信誉分详情，可选 |
| `POST /api/credit/users/{userId}/recalculate` | Path `userId` | 同上 | 有副作用的重新计算；当前只读风险页不调用 |
| `POST /api/credit/users/{userId}/documents` | Body `{ documentTypes: ["education", "income", "property", "social"] }` | 同上，并含 `message` | 用户增信资料提交，非当前管理员报告主接口 |

失败均为 HTTP 400，响应 `{ success: false, message }`。

## 7. 管理员相关接口

### 7.1 AdminController（认证）

| 方法与路径 | 参数 | Authorization | JSON 响应 | 对应页面 |
|---|---|---|---|---|
| `POST /api/admin/login` | Body `{ username, password }`，均必填 | 否 | 直接返回 `{ success, message, token, expiresIn, admin: { id, username, name, role } }` | 登录页 |
| `GET /api/admin/check-auth` | Header Bearer Token | 是 | `{ success, message, user: { id, username, name, role, loginTime } }`；无效时 HTTP 401 | 路由守卫 |
| `GET /api/admin/current` | Header Bearer Token | 是 | `{ success: true, admin: { id, username, name, role, loginTime } }` | 顶栏用户信息（可选） |
| `POST /api/admin/logout` | Header Bearer Token | 是 | `{ success, message }` | 退出登录 |

### 7.2 AdminUserController（管理端用户）

| 方法与路径 | 参数 | JSON 响应 | 对应页面 |
|---|---|---|---|
| `GET /api/admin/users` | Query `search?` | `{ success, data: AdminUserViewDTO[], total }` | 用户管理 |
| `POST /api/admin/users` | Body `{ name, phone, email, status }` | `{ success, data: AdminUserViewDTO, message }` | 新增用户 |
| `PUT /api/admin/users/{id}` | Path `id` + 同上 Body | `{ success, data: AdminUserViewDTO, message }` | 编辑用户 |
| `DELETE /api/admin/users/{id}` | Path `id` | `{ success, message }` | 删除用户 |

`AdminUserViewDTO` 字段：`id`、`name`、`phone`、`email`、`regTime`、`status`、`statusText`。全部需要 Bearer Token。

### 7.3 AdminContractController（管理端合同）

| 方法与路径 | 参数 | JSON 响应 | 对应页面 |
|---|---|---|---|
| `GET /api/admin/contracts` | Query `status?`、`search?` | `{ success, data: AdminContractViewDTO[], total }` | 合同列表 |
| `GET /api/admin/contracts/{id}` | Path `id` | `{ success, data: AdminContractViewDTO }` | 合同详情 |
| `POST /api/admin/contracts/from-application/{applicationId}` | Path `applicationId` | `{ success, message, data: AdminContractViewDTO }` | 从已批准申请生成合同 |
| `POST /api/admin/contracts/{id}/sign` | Path `id` | `{ success, message, data: AdminContractViewDTO }` | 确认签署 |
| `POST /api/admin/contracts/{id}/disburse` | Path `id` | `{ success, message, data: AdminContractViewDTO }` | 执行放款 |

`AdminContractViewDTO` 字段：`id`、`loanId`、`applicant`、`contractNo`、`amount`、`term`、`signDate`、`status`、`statusText`、`downloadUrl`、`disburseDate`、`disburseAmount`、`canGenerate`、`canSign`、`canDisburse`。全部需要 Bearer Token。

### 7.4 AdminCrawlerController

- **方法/路径**：`POST /api/admin/crawler/run`
- **参数**：无
- **成功响应**：`{ success: true, data: { creditRows, telecomRows, message } }`
- **失败响应**：HTTP 500，`{ success: false, message }`
- **Authorization**：需要 Bearer Token
- **用途**：管理端手动刷新外部数据源；有写缓存副作用，不属于当前四个页面的自动请求

## 8. 联调前必须确认的问题

1. **贷款状态与文案不一致**：后端返回/接收小写，当前贷款管理 Mock 使用大写枚举；后端 `pending` 的 `statusText` 是“审核中”，当前 Mock 是“待审核”。建议 API 适配层统一映射，不直接修改展示组件中的业务文案。
2. **产品状态不是 1/0**：后端当前使用 `active/inactive`；产品页面即使兼容显示 `1/0`，提交应使用后端实际字符串。
3. **风险分方向相反**：真实 `overallRiskScore/riskScore` 是风险分，数值越高风险越高；评分卡将 `<30` 定义为等级 1，`>=80` 为等级 4。阶段4 Mock 的“综合评分越高风险越低”不能直接套用真实 `riskScore`。应明确 UI 展示的是“风险分”还是转换后的“信用/安全分”。
4. **风险等级是整数**：真实 `overallRiskLevel/riskLevel` 为 `1..4`，不是 `LOW/MEDIUM/HIGH`。需定义前端映射规则。
5. **GET assessment 有写入副作用**：没有报告时会自动执行并保存评估。若详情页必须严格只读，应先请求 `/report`，或由后端提供无副作用的详情查询。
6. **筛选组合有限制**：产品列表只会优先应用 `type`，贷款列表只会优先应用 `status`；其他筛选条件可能被忽略。
7. **错误状态不统一**：资源不存在通常返回 400 而非 404；风险摘要“无报告”返回 HTTP 200 且 `success=false`。前端必须同时判断 HTTP 状态和 `success`。
8. **响应结构不完全统一**：`/api/risk/assess`、管理员登录、部分还款接口与常见 `success/data` 包装不同，API 函数应逐接口解包。
9. **敏感字段**：`LoanApplicationDTO` 返回完整 `idCardNumber`、`phoneNumber`。管理端展示前需确认脱敏责任位于后端还是前端。
10. **权限粒度**：当前 Security 配置只验证 JWT，不校验 admin 角色。上线或真实联调前应由后端负责人确认管理接口权限边界；本报告不修改该配置。

## 9. 推荐前端接入顺序

1. 保持现有登录与 `request()` Bearer 逻辑，先验证 `check-auth`。
2. 产品页替换为 `/api/products`，确认 `active/inactive` 与组合筛选行为。
3. 贷款列表接入 `/api/loan-applications`，在 API 层完成大小写状态映射。
4. 详情接入 `/api/loan-applications/{id}`，对证件号/手机号做脱敏确认。
5. 风险页先决定真实分数语义；需要只读时优先 `/report`，需要完整规则明细时使用 `/assessment` 并知晓其自动评估副作用。

## 10. 主要源码依据

- `config/SecurityConfig.java`、`config/JwtAuthenticationFilter.java`
- `modules/auth/controller/AdminController.java`
- `modules/product/controller/ProductController.java`、`ProductServiceImpl.java`、`ProductDTO.java`
- `modules/loanapplication/controller/LoanApplicationController.java`、`LoanApplicationServiceImpl.java`、`LoanApplicationDTO.java`
- `modules/risk/controller/RiskController.java`、`CreditController.java`
- `modules/risk/service/impl/RiskServiceImpl.java`、`ScoringCardRiskEvaluator.java`、`ExternalDataRiskEvaluator.java`
- `controller/AdminUserController.java`、`AdminContractController.java`
- `modules/crawler/controller/AdminCrawlerController.java`
- `common/exception/GlobalExceptionHandler.java`
