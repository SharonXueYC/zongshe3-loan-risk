# API 网关（平台 · 薛雨宸）

独立 Spring Boot 进程，默认端口 **8088**。APP / Web 只连这个地址。

架构口径（老师：两个业务服务）：

| 进程 | 端口 | 说明 |
|------|------|------|
| 本网关 | 8088 | 统一入口，不算业务服务 |
| 主业务 `backend/` | 8080 | 信贷 + 平台同进程 |
| 风控 `risk-service`（后拆） | 8081 | 多模型评估 |

当前路由：

- `/api/risk/**`、`/api/credit/**`、`/api/admin/crawler/**` → `RISK_SERVICE_URI`（默认仍是 8080）
- 其余业务 API → `MAIN_SERVICE_URI`（默认 8080）

风控从主业务迁出后：

```powershell
$env:RISK_SERVICE_URI="http://127.0.0.1:8081"
# 然后启动网关
```

不要再新建 `platform-service`。
