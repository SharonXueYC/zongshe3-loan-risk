# 外部公示数据源说明

## 1. 失信公示

数据源：泰州中院失信被执行人公告

URL：

https://tzcourt.taizhou.gov.cn/jbjjzxnzl/sxbzxrpgt/art/2026/art_00f7520de1d4470bbb70bd84cb06293d.html

用途：

用于获取公开失信公告数据，为风控外部公示模型提供失信命中特征。

目标字段：

- dishonestName
- courtName
- publishDate
- caseNumber
- dishonestHit

可用性验证：

- 页面 HTTP 状态：200
- 无需登录
- 无验证码
- 返回静态 HTML
- robots.txt：HTTP 200，内容为空，未发现明确 Disallow 规则

计划解析方式：

1. HTTP GET 获取公告 HTML。
2. 使用 Jsoup 解析页面。
3. 从正文或表格中提取失信被执行人姓名。
4. 提取法院名称和发布日期。
5. 第三周按归一化姓名写入缓存。
6. 第五周失信命中进入准入硬规则，原因码为 `DISHONEST_PUBLIC`。

---

## 2. LPR 主数据源

数据源：中国货币网

URL：

https://www.chinamoney.com.cn/chinese/rdgz/20260820/3399885.html

用途：

获取公开贷款市场报价利率，作为外部公示模型中的宏观利率特征。

目标字段：

- lpr1y
- lpr5y
- lprPublishDate

可用性验证：

- 页面 HTTP 状态：200
- 无需登录
- 无验证码
- 返回 HTML
- `/robots.txt` 路径返回 `Path not found`
- 未获取到明确的 robots 禁止规则

计划解析方式：

1. HTTP GET 获取公告页面。
2. 使用 Jsoup 解析正文。
3. 从正文提取“1年期 LPR”和“5年期以上 LPR”。
4. 转换为 BigDecimal。
5. 保存发布日期。
6. 第三周写入 `MACRO_LPR` 缓存。

---

## 3. LPR 备用数据源

数据源：广发银行官网

URL：

https://www.cgbchina.com.cn/Info/22984022

用途：

当中国货币网主数据源访问失败或解析失败时作为备用 LPR 数据源。

目标字段：

- lpr1y
- lpr5y
- lprPublishDate

可用性验证：

- 页面 HTTP 状态：200
- 返回 HTML
- 页面编码：GBK
- robots.txt：HTTP 200
- `User-agent: *` 下存在 `Allow: /`
- 当前页面路径不命中 `.php`、`.asp`、`.aspx`、`.sql`、`.txt`、`.xml`、`.pdf` 等 Disallow 规则

注意：

第三周实现备用源解析时，需要正确处理 GBK 页面编码。

---

## 4. 中国人民银行官网说明

央行 LPR 页面可直接访问并返回 HTTP 200，但其 robots.txt 包含：

```text
User-agent: *
Disallow: /