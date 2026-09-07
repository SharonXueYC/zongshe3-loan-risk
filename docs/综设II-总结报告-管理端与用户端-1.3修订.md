# 综设 II · 1.3 管理端与用户端修订稿（仅改不符事实关键词）

> 对照 `frontend/`、`apps/uni-client/` 核对。下文为**全文**，**加粗处**为相对原稿修改的关键词/短语；合并 Word 时可去掉加粗。

---

## 1.3.1 管理员前端（修订全文）

管理员前端位于系统业务展示层与管理操作层之间，其主要任务是将后端复杂的贷款业务数据转化为结构清晰、易于操作的前端界面。在个人贷款风控系统中，业务流程涉及用户申请、风控评估、人工审核、合同生成以及放款管理等多个环节，数据类型复杂且状态变化频繁。如果前端设计不合理，将直接导致管理员难以快速理解业务状态，从而影响整体审批效率。

从工程本质来看，管理员前端主要面临以下几个核心问题：

（1）业务流程复杂导致信息结构割裂问题  
（原文不变）

（2）数据表达方式与业务决策需求不匹配问题  
（原文不变）

（3）前后端数据交互复杂导致一致性风险问题  
（原文不变）

（4）关键业务操作缺乏状态约束导致误操作风险  
（原文不变）

（5）风控信息表达不足导致判断效率低问题  

……因此在设计中采用多维度表达方式，包括：  
风险标签颜色区分  
风险等级结构化展示  
**控制台 ECharts 展示申请趋势、贷款状态分布等统计图表（风控明细以贷款列表标签与详情为主）**  

（6）系统扩展性不足导致维护成本增加问题  

……因此在设计中采用**模块化页面结构**，**将公共请求逻辑抽取至 api 目录**，**在 Dashboard.vue 内通过 currentPage 切换各业务区块**，从而提高系统可扩展性与维护性。

在明确上述工程问题之后，本阶段主要围绕“问题驱动设计”的思路进行前端实现，将复杂业务需求逐步转化为可运行的系统功能。

（1）**登录与权限模块实现**  
登录模块使用**自定义表单组件**实现基础交互，通过 **fetch（api/admin.js）** 向后端发送登录请求。登录成功后保存用户身份信息，**由 App.vue 根据 Token 切换登录页与主控制台**，**未使用 vue-router 路由守卫**，从而保证系统访问边界。

（2）**后台布局与模块化结构实现**  
管理员后台采用标准的**顶部导航栏 +** 左侧菜单 + 右侧内容区布局结构。左侧菜单用于功能导航，包括**控制台、用户管理、贷款管理、产品管理、还款计划、合同管理、数据统计、系统设置**等模块；右侧区域**根据 currentPage 变量**切换页面内容。  
**以 Dashboard.vue 封装整体布局**，实现页面结构统一，提高代码复用率。

（3）**贷款管理模块实现**  
贷款管理页面使用**原生 table 表格**展示申请数据，**结合筛选条件加载列表**。**在操作层面**，支持查看详情、审核通过、审核拒绝等功能。  
同时，根据申请状态动态渲染不同操作按钮，并在关键操作前加入 **window.confirm 确认**，避免误操作发生。审核完成后自动刷新数据，保证前后端状态一致。

（4）**风控与数据可视化模块实现**  
风控结果**在贷款列表与详情弹窗中**通过标签形式进行分类展示，不同风险等级使用不同颜色进行区分。同时在控制台**及数据统计页**引入 ECharts，实现贷款申请数量统计、**贷款状态分布**以及**月度还款**等可视化图表，使管理员能够快速掌握系统运行状态。

（5）**接口联调与数据一致性实现**  
通过 **fetch 在 api/client.js、api/admin.js 中**对接口进行统一封装，将请求按业务模块进行划分，减少页面重复代码。在联调过程中，根据后端接口返回结果调整字段映射关系，解决了字段不一致与数据为空的问题，保证前后端数据同步更新。

（6）**系统稳定性与交互优化实现**  
在用户操作过程中**通过 alert 提示与登录页 loading 状态**提供操作反馈，提高系统可用性。同时对关键操作（如审批、放款、删除）增加**confirm 二次确认**流程，避免误操作。

---

## 1.3.2 用户端前端（修订全文）

在具体实现中，我主要完成了用户端前端的页面搭建、交互逻辑、接口联调和功能测试工作。用户端主要包括登录注册页面、**首页（main-home.html）**、借款申请页面、额度查询页面、还款管理页面、银行卡管理页面、借款记录页面、人脸验证页面和**额度评估进度页面（review-progress.html）**。

登录页使用原生HTML表单+CSS，支持密码/验证码双模式，格式校验（手机号11位、密码≥8位），调用/api/users/login接口，成功后Token存入localStorage跳转首页。验证码登录调用/api/users/login-otp，60秒倒计时。密码输入框通过切换input.type实现显示/隐藏。注册页两步注册模式：第一步验证手机号，第二步设置密码，调用/api/users/register接口，成功后自动登录跳转隐私协议授权页。

首页从localStorage读取Token，调用/api/credit/users/{userId}/evaluation获取可借额度展示在渐变卡片中。功能菜单使用CSS Grid 4列布局，快捷操作列表**位于首页下方区域（页面跳转入口，非底部 Tab 导航）**。

借款页包含金额输入、快捷金额选择（十分之一/一半/全部）、期限选择、还款方式选择。实时计算器等额本息计算逻辑：monthly = amount * monthlyRate * (1+monthlyRate)^term / ((1+monthlyRate)^term - 1)，总利息=monthly * term - amount；等额本金逐期累加剩余本金×月利率。提交时调用/api/loan-applications/submit接口，**成功后跳转 loan-records.html?submitted=1**。

额度页使用SVG circle配合stroke-dasharray和stroke-dashoffset实现环形进度条，**通过 CSS transition 驱动 stroke-dashoffset 过渡动画**。展示可借额度、信誉等级、构成明细。增信资料通过checkbox多选、file上传，提交调用/api/credit/users/{userId}/documents接口。

图1.3.2.1 额度计算 → 建议图题改为 **图1.3.2.1 额度评估进度（review-progress.html）**

还款页分"待还计划"和"还款记录"**两个同页功能区块（非 Tab 切换）**，调用/api/repayment/plans/user和/api/repayment/records/user接口。待还计划支持全额/部分还款，点击跳转付款确认页二次确认。还款记录支持日期范围和金额筛选，前端filter方法实现。

图1.3.2.2～1.3.2.4（待还计划、还款记录、筛选）— 表述可保留，**勿写「Tab 切换」**。

银行卡管理数据存入localStorage按用户ID隔离。借款记录调用/api/loan-applications/user/{userId}，进度条通过CSS width控制，**状态优先展示后端 statusText**。人脸验证**通过 setTimeout 分步模拟引导（H5 演示，未调用真实摄像头）**。  
**user-session.js 中基于 fetch 封装 apiRequest**，**当前尚未实现 AbortController 全局超时**；未登录时 **ensureSessionOrRedirect 跳转登录页**，**/?logout=1 可清除登录态**。**Toast 为各页面内置 showToast**，**未抽离为统一 Modal/Loading 组件库**。

联调过程中根据后端返回调整字段映射，处理数据为空、状态值不一致、刷新不及时等问题。

在联调过程中，我根据后端接口返回结果调整前端字段映射，处理了数据为空、状态值不一致、操作后页面未及时刷新等问题。经过多次测试和修改，用户端前端基本能够支撑系统的完整演示流程。

---

## 关键词修订对照表（1.3 专用）

### 管理端 1.3.1

| 原关键词 | 应改为 |
|----------|--------|
| Element Plus 表单 | 自定义表单（Login.vue / Dashboard.vue） |
| Axios | fetch；api/client.js、api/admin.js |
| 路由守卫 | App.vue Token 判断切换 Login/Dashboard |
| Layout 组件 + 路由动态切换 | Dashboard.vue + currentPage |
| 贷款申请管理、风控管理（独立） | 贷款管理（含风控列/详情）；另含用户/还款/统计/设置 |
| 表格组件 + 分页 | 原生 table + 筛选列表（无分页组件） |
| 确认弹窗（Element） | window.confirm / 自定义 modal |
| 风险等级分布图表 | 贷款状态分布、申请趋势等；风控以列表标签为主 |
| 路由模块管理页面 | currentPage 模块切换 |
| 加载状态提示（通用组件） | alert/confirm；登录页 loading 标志 |

### 用户端 1.3.2

| 原关键词 | 应改为 |
|----------|--------|
| 首页控制台 | 首页 main-home.html |
| 底部（暗示 Tab） | 首页下方快捷列表；非底部 Tab |
| requestAnimationFrame | CSS transition + stroke-dashoffset |
| Tab 切换 | 同页两个功能区块 |
| 额度计算页面 | review-progress.html 额度评估进度 |
| 提交成功（未写跳转） | loan-records.html?submitted=1 |
| request + AbortController 超时 | apiRequest（fetch）；无全局超时 |
| 401 自动清除跳转 | ensureSessionOrRedirect；/?logout=1 |
| Toast/Modal/Loading 多页复用 | 各页 showToast；无统一组件库 |
| 人脸验证 | setTimeout 模拟（正确，保留） |

### 无需修改（与原稿一致）

- /api/users/login、login-otp、register、loan-applications/submit、documents、repayment 等接口路径  
- 等额本息/等额本金公式  
- Grid 4 列、localStorage 银行卡、filter 筛选、setTimeout 人脸步骤  
- 管理端问题（1）～（4）理论分析段落  

---

*答辩口径：管理端 **Vue3 + ECharts + fetch + 自定义 CSS**；用户端 **HTML MPA + fetch**。*
