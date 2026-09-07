#!/usr/bin/env python3
"""
演示用外部数据爬取脚本（课程项目）
解析与后端相同的 HTML 表格结构，可通过管理端接口触发 Java 爬取，或独立验证页面结构。

用法:
  python crawl_external_data.py
  python crawl_external_data.py --backend http://localhost:8080 --trigger-api
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

try:
    import requests
    from bs4 import BeautifulSoup
except ImportError:
    print("请先安装依赖: pip install requests beautifulsoup4")
    sys.exit(1)

ROOT = Path(__file__).resolve().parent.parent
CREDIT_HTML = ROOT / "backend" / "src" / "main" / "resources" / "crawler" / "demo-credit.html"
TELECOM_HTML = ROOT / "backend" / "src" / "main" / "resources" / "crawler" / "demo-telecom.html"


def parse_credit_table(html: str) -> list[dict]:
    soup = BeautifulSoup(html, "html.parser")
    rows = soup.select("table#credit-data tbody tr")
    result = []
    for row in rows:
        cols = [c.get_text(strip=True) for c in row.select("td")]
        if len(cols) >= 3:
            result.append({
                "id_suffix": cols[0],
                "credit_overdue_count": int(cols[1]),
                "credit_query_count_30d": int(cols[2]),
            })
    return result


def parse_telecom_table(html: str) -> list[dict]:
    soup = BeautifulSoup(html, "html.parser")
    rows = soup.select("table#telecom-data tbody tr")
    result = []
    for row in rows:
        cols = [c.get_text(strip=True) for c in row.select("td")]
        if len(cols) >= 3:
            result.append({
                "phone_suffix": cols[0],
                "telecom_online_months": int(cols[1]),
                "telecom_real_name_verified": cols[2].lower() == "true",
            })
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description="外部数据爬取演示")
    parser.add_argument("--backend", default="http://localhost:8080", help="后端地址")
    parser.add_argument("--trigger-api", action="store_true", help="调用 POST /api/admin/crawler/run")
    args = parser.parse_args()

    credit = parse_credit_table(CREDIT_HTML.read_text(encoding="utf-8"))
    telecom = parse_telecom_table(TELECOM_HTML.read_text(encoding="utf-8"))
    print(f"本地解析：征信 {len(credit)} 行，运营商 {len(telecom)} 行")
    print("征信样本:", credit[:2])
    print("运营商样本:", telecom[:2])

    if args.trigger_api:
        url = f"{args.backend.rstrip('/')}/api/admin/crawler/run"
        # 需管理员 JWT；演示环境可先登录获取 token 后设置环境变量 ADMIN_TOKEN
        token = __import__("os").environ.get("ADMIN_TOKEN", "")
        headers = {"Authorization": f"Bearer {token}"} if token else {}
        resp = requests.post(url, headers=headers, timeout=30)
        print("API 响应:", resp.status_code, resp.text[:500])


if __name__ == "__main__":
    main()
