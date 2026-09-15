# 风控爬虫接口契约（第二周）

本文档仅用于第二周联调，说明两个公开数据源的字段约定。字段名与 DTO 保持一致，不会改动第一周已定义的 DTO 与 `RiskRuleEvaluator`。

## 1. 失信详情页字段

返回字段示例：

```json
{
  "dishonestHit": true,
  "dishonestName": "张三",
  "courtName": "泰州市中级人民法院",
  "publishDate": "2026-06-22",
  "caseNumber": "(2026)台中执字第123号",
  "sourceUrl": "https://tzcourt.taizhou.gov.cn/..."
}
```

## 2. LPR 公告字段

返回字段示例：

```json
{
  "lpr1y": 3.65,
  "lpr5y": 4.30,
  "lprPublishDate": "2026-08-20",
  "sourceUrl": "https://www.chinamoney.com.cn/..."
}
```

## 3. 说明

- 这两组字段都适合直接映射到 `FeatureSnapshotDTO`，用于第二周展示 `dishonestHit` / `lpr1y` / `lpr5y` 等字段。
- 若页面结构变动，则更新 `PublicSourceDemoCrawlerService` 中对应解析逻辑，不改动第一周旧契约。
- 本周仍保持演示优先，不做真实风控评估。
