# 风控爬虫选择器草案（第二周）

## 1. 失信公示详情页（首选）

URL：
https://tzcourt.taizhou.gov.cn/jbjjzxnzl/sxbzxrpgt/art/2026/art_00f7520de1d4470bbb70bd84cb06293d.html

目标字段：
- dishonestHit
- dishonestName
- courtName
- publishDate
- caseNumber

建议选择器：
- 标题/法院：`div.title, h1, div.header`
- 日期：`*:matchesOwn(\d{4}[-年]\d{1,2}[-月]\d{1,2})`
- 关键词：`被执行人`, `案号`

落点：
- `dishonestName`：文本中出现 `被执行人` 后的值
- `courtName`：页面标题 / 表头机构名
- `publishDate`：正文中首次出现的日期
- `caseNumber`：文本中出现 `案号` 后的值

## 2. LPR 公告（首选）

URL：
https://www.chinamoney.com.cn/chinese/rdgz/20260820/3399885.html

目标字段：
- lpr1y
- lpr5y
- lprPublishDate

建议选择器：
- 日期：`body` 正文中的 `\d{4}年\d{1,2}月\d{1,2}日` 或 `\d{4}-\d{1,2}-\d{1,2}`
- 关键词：`1年期`, `5年期`

落点：
- `lpr1y`：正文中 `1年期` 后的数字
- `lpr5y`：正文中 `5年期` 后的数字
- `lprPublishDate`：正文中首次出现的日期

## 3. 备注

- 这两条页面都无需登录、无需验证码，适合第二周现场演示。
- 解析器设计为“字段归一 + 回退值”，避免联调时因页面格式差异导致页面崩掉。
- 生产环境中，再增加重试、超时和 selector 适配器。