#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

if ! command -v docker >/dev/null 2>&1; then
  echo "请先安装 Docker 与 Docker Compose"
  exit 1
fi

if [ ! -f .env ]; then
  cp .env.example .env
  echo "已生成 deploy/.env，请编辑其中的密码与 JWT_SECRET 后重新运行："
  echo "  bash deploy/deploy.sh"
  exit 1
fi

# shellcheck disable=SC1091
source .env

if [ "${DB_PASSWORD:-}" = "请修改为强密码" ] || [ "${JWT_SECRET:-}" = "请替换为至少32位随机字符串" ]; then
  echo "请先在 deploy/.env 中修改 DB_PASSWORD、JWT_SECRET、ADMIN_PASSWORD"
  exit 1
fi

echo ">>> 构建并启动服务..."
docker compose up -d --build

echo ""
echo ">>> 部署完成"
echo "  手机用户端: http://<服务器IP>/"
echo "  管理后台:   http://<服务器IP>/admin/"
echo "  API:        http://<服务器IP>/api/"
echo ""
echo "查看日志: docker compose logs -f"
echo "停止服务: docker compose down"
